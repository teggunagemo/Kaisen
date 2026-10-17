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
        System.out.println("WebSocketゲームサーバーがポート " + getPort() + " で起動しました。");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        players.add(conn);
        System.out.println("プレイヤー入室。現在: " + players.size() + "人");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        players.remove(conn);
        System.out.println("プレイヤー退室。現在: " + players.size() + "人");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
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
        int port = 10000; // Renderの固定ポート
        String host = "0.0.0.0";

        System.out.println("WebSocket単体での軽量起動。ポート: " + port);
        GameServer server = new GameServer(host, port);
        server.setReuseAddr(true);
        
        try {
            // メンスレッドを占有してプロセスを確実に維持する（一番確実な方法）
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
