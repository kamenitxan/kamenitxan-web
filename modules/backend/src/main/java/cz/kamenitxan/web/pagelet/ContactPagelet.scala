package cz.kamenitxan.web.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractJsonPagelet, JsonPagelet, Post}
import cz.kamenitxan.jakon.core.dynamic.entity.{AbstractJsonResponse, EmptyJsonResponse, JsonFailResponse}
import cz.kamenitxan.jakon.utils.mail.EmailEntity
import io.javalin.http.Context

@JsonPagelet(path = "/api/contact")
class ContactPagelet extends AbstractJsonPagelet {

	@Post(path = "")
	def submit(ctx: Context): AbstractJsonResponse[?] = {
		val name = Option(ctx.formParam("jmeno")).getOrElse("").trim
		val email = Option(ctx.formParam("email")).getOrElse("").trim
		val message = Option(ctx.formParam("body")).getOrElse("").trim

		if (name.isEmpty || email.isEmpty || message.isEmpty) {
			return new JsonFailResponse("Vyplňte prosím všechna pole.")
		}

		val entity = new EmailEntity(
			"contact",
			"info@kamenitxan.eu",
			s"Nová poptávka od $name",
			Map("name" -> name, "email" -> email, "message" -> message)
		)
		entity.create()

		new EmptyJsonResponse()
	}
}

