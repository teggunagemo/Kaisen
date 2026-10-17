import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer extends WebSocketServer {

    private final List<WebSocket> players = new CopyOnWriteArrayList<>();

    // 変更点: 引数でホスト（0.0.0.0）とポートを受け取るようにする
    public GameServer(String host, int port) {
        super(new InetSocketAddress(host, port));
    }

    @Override
    public void onStart() {
        System.out.println("Render対応サーバーがポート " + getPort() + " で起動しました。");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        players.add(conn);
        System.out.println("プレイヤーが入室しました。現在のプレイヤー数: " + players.size());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        players.remove(conn);
        System.out.println("プレイヤーが退室しました。現在のプレイヤー数: " + players.size());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("メッセージ受信: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("エラー: " + ex.getMessage());
    }

    public static void main(String[] args) {
        // 【重要】Renderから渡される環境変数PORTを取得する
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 8887; // なければローカル用に8887

        // 【重要】外部アクセスのためホストは "0.0.0.0" に固定する
        String host = "0.0.0.0"; 

        System.out.println("起動ポートを設定中: " + port);
        GameServer server = new GameServer(host, port);
        server.start();
    }
}
