import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer extends WebSocketServer {

    private final List<WebSocket> players = new CopyOnWriteArrayList<>();

    public GameServer(String host, int port) {
        super(new InetSocketAddress(host, port));
    }

    @Override
    public void onStart() {
        System.out.println("Zeabur対応サーバーがポート " + getPort() + " で起動しました。");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        players.add(conn);
        System.out.println("プレイヤーが入室。現在: " + players.size() + "人");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        players.remove(conn);
        System.out.println("プレイヤーが退室。現在: " + players.size() + "人");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // 【自動スリープ対策用】クライアントからの生存確認（Ping）に反応する
        if ("ping".equals(message)) {
            conn.send("pong");
            return;
        }
        System.out.println("受信: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("エラー: " + ex.getMessage());
    }

    public static void main(String[] args) {
        // Renderの標準ポート 10000 に固定
        int port = 10000; 
        String host = "0.0.0.0"; 

        System.out.println("Render用ポート固定設定: " + port);
        GameServer server = new GameServer(host, port);
        
        // 修正ポイント①: 起動時のポート衝突を防ぐため、再利用設定を有効化する
        server.setReuseAddr(true);
        
        // 修正ポイント②: バックグラウンドで非同期にサーバーを開始（Renderがポートをスキャンできるようにする）
        System.out.println("サーバーをバックグラウンドで開始します...");
        server.start();

        // 修正ポイント③: Renderのシステムに「起動完了」を伝えるため、メインスレッドを終了させず永続的に眠らせる
        System.out.println("ポート " + port + " での起動に成功しました。常時接続待機モードに入ります。");
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 1分ごとに生存ログを出しつつ、スレッドを維持する（Renderに生きてることをアピール）
                Thread.sleep(60000); 
                System.out.println("[Keep-Alive] サーバーは正常に稼働中です...");
            } catch (InterruptedException e) {
                System.err.println("メインスレッドが中断されました: " + e.getMessage());
                break;
            }
        }
    }
}
