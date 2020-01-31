package net.perfectdreams.spicymorenitta.utils

import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import kotlin.browser.window

object DashboardUtils {
	@ImplicitReflectionSerializer
	suspend fun retrieveGuildConfiguration(guildId: Long) = retrieveGuildConfiguration(guildId.toString())

	@ImplicitReflectionSerializer
	suspend fun retrieveGuildConfiguration(guildId: String): ServerConfig.Guild {
		val result = http.get<String> {
			url("${window.location.origin}/api/v1/guild/${guildId}/config")
		}

		return kotlinx.serialization.json.JSON.nonstrict.parse(result)
	}

	fun UpdateNavbarSizePostRender.switchContentAndFixLeftSidebarScroll(call: ApplicationCall) {
		fixDummyNavbarHeight(call)
		SpicyMorenitta.INSTANCE.fixLeftSidebarScroll {
			switchContent(call)
		}
	}

	fun UpdateNavbarSizePostRender.launchWithLoadingScreenAndFixContent(call: ApplicationCall, callback: suspend CoroutineScope.() -> (Unit)) {
		SpicyMorenitta.INSTANCE.launch {
			SpicyMorenitta.INSTANCE.showLoadingScreen()
			callback.invoke(this)
			SpicyMorenitta.INSTANCE.hideLoadingScreen()
		}
	}
}