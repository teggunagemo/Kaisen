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

        // 外部アクセスを許可するため 0.0.0.0 を指定
        String host = "0.0.0.0"; 

        System.out.println("Render用ポート固定設定: " + port);
        GameServer server = new GameServer(host, port);
        
        // サーバーを裏で開始（これだけだとメインスレッドが終了してしまう）
        server.start();

        // 【重要】Render上でプログラムが終了しないように、無限ループでスレッドを維持する
        System.out.println("サーバーが起動しました。接続を待機しています...");
        while (true) {
            try {
                // 1時間（3600000ミリ秒）寝かせるのを繰り返して、プログラムを常時起動させる
                Thread.sleep(3600000); 
            } catch (InterruptedException e) {
                System.err.println("スレッドが中断されました: " + e.getMessage());
                break;
            }
        }
    }
}
