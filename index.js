const { WebSocketServer } = require('ws');
// cryptoモジュールが明示的に必要（Node.jsのバージョンによって必須）
const crypto = require('crypto'); 

// Renderが自動で割り当てるポート（10000番など）を読み込む
const port = process.env.PORT || 10000;
const wss = new WebSocketServer({ port: port });

// 接続中のプレイヤー一覧を管理するリスト
const players = new Map();

wss.on('listening', () => {
    console.log(`Node.jsゲームサーバーがポート ${port} で正常に起動しました！`);
});

wss.on('connection', (ws) => {
    // プレイヤーをリストに追加
    const guestId = `guest_${crypto.randomUUID()}`;

    // 2. ソケット自身にIDを覚えさせておく（切断時に使うため）
    ws.userId = guestId;
    players.set(guestId, ws);

    console.log(`プレイヤーが入室しました。現在の接続数: ${players.size}人`);

    // メッセージを受信したときの処理
    ws.on('message', (message) => {
        const msgStr = message.toString();
        
        // 【自動スリープ対策】クライアントからのpingに即座にpongを返す
        if (msgStr === 'ping') {
            ws.send('pong');
            return;
        }
        
        console.log(`受信データ: ${msgStr}`);
        
        // 【修正1】Javaクライアントにパケットをそのまま転送するブロードキャスト処理
        // Mapのループ処理を正しく修正しました
        for (let [id, client] of players) {
            // 自分以外のプレイヤー、かつ接続が正常なクライアントに送る
            if (id !== ws.userId && client.readyState === 1) {
                client.send(msgStr);
            }
        }
    });

    // 接続が切れたときの処理
    ws.on('close', () => {
        const userId = ws.userId;
        
        if (userId) {
            // 【修正2】正しく変数「players」から削除するよう修正
            players.delete(userId);
            console.log(`ユーザー ${userId} が切断。現在の合計: ${players.size}人`);
        }
    });

    // エラーが起きたときの処理
    ws.on('error', (error) => {
        console.error(`通信エラーが発生しました: ${error.message}`);
    });
});
