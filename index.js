const { WebSocketServer } = require('ws');
const crypto = require('crypto'); 

const port = process.env.PORT || 10000;
const wss = new WebSocketServer({ port: port });

// 【新構造】部屋の一覧を管理するMap (Key: 部屋ID, Value: 参加プレイヤーのSet)
const rooms = new Map();

// プレイヤーIDと所属している部屋IDを紐付けるMap (切断時の処理を高速化するため)
const playerRoomMap = new Map();

wss.on('listening', () => {
    console.log(`ゲームサーバーがポート ${port} で起動しました（部屋機能付き）`);
});

// 空いている部屋を探す、または新しく部屋を作る関数
function getOrCreateRoom() {
    // 既存の部屋で、まだ4人未満の部屋を探す
    for (let [roomId, playerSet] of rooms) {
        if (playerSet.size < 4) {
            return roomId;
        }
    }
    // 満室、または部屋が1つもない場合は新しい部屋IDを作って登録
    const newRoomId = `room_${crypto.randomUUID().substring(0, 8)}`; // 短い一意のID
    rooms.set(newRoomId, new Set());
    return newRoomId;
}

wss.on('connection', (ws) => {
    const guestId = `guest_${crypto.randomUUID()}`;
    ws.userId = guestId;

    // 1. 自動で部屋を割り振る
    const roomId = getOrCreateRoom();
    const currentRoom = rooms.get(roomId);

    // 2. 部屋にプレイヤー(ws)を追加する
    currentRoom.add(ws);
    ws.roomId = roomId; // ソケット自身にも部屋IDを覚えさせる
    playerRoomMap.set(guestId, roomId);

    console.log(`プレイヤー ${guestId} が室 [${roomId}] に入室しました。現在の部屋人数: ${currentRoom.size}人`);

    ws.on('message', (message) => {
        const msgStr = message.toString();
        
        if (msgStr === 'ping') {
            ws.send('pong');
            return;
        }
        
        console.log(`[${ws.roomId}] 受信: ${msgStr}`);
        
        // 【重要】同じ部屋（同じ配列/Set内）のメンバーだけにブロードキャスト（転送）する
        const targetRoom = rooms.get(ws.roomId);
        if (targetRoom) {
            for (let client of targetRoom) {
                // 自分以外の、接続が正常なプレイヤーにのみ送る
                if (client !== ws && client.readyState === 1) {
                    client.send(msgStr);
                }
            }
        }
            // 1. 自動で部屋を割り振る
    const roomId = getOrCreateRoom();
    const currentRoom = rooms.get(roomId);

    // 2. 部屋にプレイヤー(ws)を追加する
    currentRoom.add(ws);
    ws.roomId = roomId; // ソケット自身にも部屋IDを覚えさせる
    playerRoomMap.set(guestId, roomId);

    console.log(`プレイヤー ${guestId} が室 [${roomId}] に入室しました。現在の部屋人数: ${currentRoom.size}人`);

    // ⭐【追加】部屋が4人（満室）になった瞬間に、その部屋の全員に「5」を送る
    if (currentRoom.size === 4) {
        console.log(`室 [${roomId}] が満室になりました！全員にパケット「5」を送信します。`);
        for (let client of currentRoom) {
            if (client.readyState === 1) { // 接続が正常なプレイヤー全員に送信
                client.send('5');
            }
        }
    }
    });

    ws.on('close', () => {
        const userId = ws.userId;
        const rId = ws.roomId;
        
        if (rId && rooms.has(rId)) {
            const targetRoom = rooms.get(rId);
            // 部屋のリスト（配列/Set）からプレイヤーを削除
            targetRoom.delete(ws);
            playerRoomMap.delete(userId);
            
            console.log(`ユーザー ${userId} が室 [${rId}] から退室。部屋の残り人数: ${targetRoom.size}人`);
            
            // もし部屋に誰もいなくなったら、メモリ節約のために部屋ごと削除する
            if (targetRoom.size === 0) {
                rooms.delete(rId);
                console.log(`室 [${rId}] は空になったため解体されました。`);
            }
        }
    });

    ws.on('error', (error) => {
        console.error(`通信エラー: ${error.message}`);
    });
});
