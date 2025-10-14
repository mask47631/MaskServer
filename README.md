# MaskServer
Mask服务



1. 添加了WebSocket依赖
   在pom.xml中添加了spring-boot-starter-websocket依赖项，这是使用WebSocket功能所必需的。
2. 创建了两种WebSocket实现方式
   方式一：基础WebSocket API
   创建了ChatWebSocketHandler.java用于处理基本的WebSocket连接
   在WebSocketConfig.java中注册了该处理器，可通过/ws/chat端点访问
   方式二：STOMP WebSocket（推荐）
   创建了WebSocketStompConfig.java用于配置STOMP消息代理
   创建了WebSocketController.java用于处理STOMP消息
   提供了公共聊天和私聊功能
3. 创建了测试页面
   创建了websocket-test.html文件，可以通过浏览器访问该页面来测试WebSocket连接。
   如何测试WebSocket功能
   启动您的应用程序
   在浏览器中访问 http://localhost:8080/websocket-test.html（假设您的应用运行在默认端口）
   点击"连接"按钮建立WebSocket连接
   在输入框中输入消息并点击"发送"进行测试
   可用的WebSocket端点
   基础WebSocket: ws://localhost:8080/ws/chat
   STOMP WebSocket: ws://localhost:8080/ws (需要使用SockJS和STOMP客户端)
   使用说明
   公共聊天: 发送消息到 /app/chat，订阅 /topic/messages
   私聊: 发送消息到 /app/private，接收方订阅 /queue/messages
   这些实现提供了完整的WebSocket功能，您可以根据具体需求选择使用其中一种或两种方式。STOMP方式更加灵活且功能丰富，适合复杂的消息传递场景。