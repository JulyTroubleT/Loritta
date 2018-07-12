package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Guild

class EconomyPayload : ConfigPayloadType("economy") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		applyReflection(payload, serverConfig.economyConfig)
	}
}