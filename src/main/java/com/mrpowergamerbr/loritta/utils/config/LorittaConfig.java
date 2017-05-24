package com.mrpowergamerbr.loritta.utils.config;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class LorittaConfig {
	public String clientToken;
	public String clientId;
	public String clientSecret;
	public String youtubeKey;
	public String ownerId;
	public String websiteUrl;
	public String frontendFolder;
	public String mercadoPagoClientId;
	public String mercadoPagoClientToken;
	public String currentlyPlaying;
}
