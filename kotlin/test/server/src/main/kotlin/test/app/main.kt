package test.app

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.logging.LogLevel
import org.shypl.biser.csi.Address
import org.shypl.biser.csi.server.ClientFactory
import org.shypl.biser.csi.server.ExecutorsProvider
import org.shypl.biser.csi.server.Server
import org.shypl.biser.csi.server.ServerSettings
import org.shypl.biser.csi.server.netty.NettyChannelGate
import test.csi.SessionUser
import test.csi.api.Api
import test.csi.api.Client
import test.csi.api.ServiceSession
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

fun main(args: Array<String>) {
	
	val executorsProvider = object : ExecutorsProvider {
		private val service = Executors.newScheduledThreadPool(2)
		
		override fun getServerExecutorService(): ScheduledExecutorService = service
		override fun getClientExecutorService(): ScheduledExecutorService = service
		override fun getConnectionExecutorService(): ScheduledExecutorService = service
	}
	
	val api = Api<TestClient>(TestClientFactory()).apply {
		setServiceSession(ServiceSessionImpl())
	}
	
	val server = Server(
		executorsProvider,
		NettyChannelGate(NioEventLoopGroup(1), NioEventLoopGroup(), LogLevel.TRACE),
		ServerSettings(Address.factoryWebSocket("localhost:8888")),
		api
	)
	
	server.start()
	
	Runtime.getRuntime().addShutdownHook(
		Thread(
			Runnable(server::stop),
			"shutdown"
		)
	)
}


class TestClientFactory : ClientFactory<TestClient> {
	override fun factoryClient(authorizationKey: String): TestClient {
		return TestClient(1)
	}
}

class TestClient(id: Long) : Client(id) {

}

class ServiceSessionImpl : ServiceSession<TestClient> {
	override fun getUser(client: TestClient): SessionUser {
		return SessionUser().apply {
			id = 777
			balance = Long.MAX_VALUE // 9_223_372_036_854_775_807
		}
	}
}