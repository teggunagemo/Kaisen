const { WebSocketServer } = require('ws');

// Renderが自動で割り当てるポート（10000番など）を読み込む
const port = process.env.PORT || 10000;
const wss = new WebSocketServer({ port: port });

// 接続中のプレイヤー一覧を管理するリスト
const players = new Set();

wss.on('listening', () => {
    console.log(`Node.jsゲームサーバーがポート ${port} で正常に起動しました！`);
});

wss.on('connection', (ws) => {
    // プレイヤーをリストに追加
    players.add(ws);
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
        
        // （例）受信したメッセージを、他の全プレイヤーにそのまま転送（ブロードキャスト）する場合
        // for (let player of players) {
        //     if (player !== ws && player.readyState === 1) {
        //         player.send(msgStr);
        //     }
        // }
    });

    // 接続が切れたときの処理
    ws.on('close', () => {
        players.delete(ws);
        console.log(`プレイヤーが退室しました。現在の接続数: ${players.size}人`);
    });

    // エラーが起きたときの処理
    ws.on('error', (error) => {
        console.error(`通信エラーが発生しました: ${error.message}`);
    });
});
