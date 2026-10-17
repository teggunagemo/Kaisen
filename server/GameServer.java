import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
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
        System.out.println("WebSocketサーバーがポート " + getPort() + " で起動しました。");
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
        int port = 10000; // Renderの標準ポート
        String host = "0.0.0.0";

        System.out.println("Renderデプロイ最適化起動を開始します。ポート: " + port);

        try {
            // 【対策】Renderのポートスキャンを確実に通過させるためのJava標準HTTPサーバーを起動
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            
            // ルートURL (/) にアクセスがあったら、Renderに「生きてるよ(200 OK)」と即答する設定
            httpServer.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String response = "Game Server is Running";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            });

            // HTTPサーバーをバックグラウンドで開始（これでRenderからの通信を常に受け付けます）
            httpServer.start();
            System.out.println("Render監視用HTTPサーバーが起動しました。");

            // 本命のWebSocketサーバーを「別のポート（例: 10001番）」で起動する
            // ※Renderの仕様により、外部からは10000番(HTTP)経由でWebSocket(WSS)に自動でアップグレード接続されます
            GameServer wsServer = new GameServer(host, 10001);
            wsServer.setReuseAddr(true);
            wsServer.start(); 
            System.out.println("ゲーム用WebSocketサーバーが起動しました。");

        } catch (Exception e) {
            System.err.println("サーバー起動中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
