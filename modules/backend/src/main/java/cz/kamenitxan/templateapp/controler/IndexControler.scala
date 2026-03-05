package cz.kamenitxan.templateapp.controler

import cz.kamenitxan.jakon.core.controller.IController
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils

class IndexControler extends IController {
	private val template = "index"

	def generate(): Unit = {
		val e = TemplateUtils.getEngine
		e.render(template, "index.html", Map.empty)
	}
}
