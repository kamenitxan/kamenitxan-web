package cz.kamenitxan.web

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.web.controler.{IndexControler, NetworkingPage, ProgrammingPage, SmartHomePage}

/**
  * Created by TPa on 2019-08-24.
  */
class AppInit extends JakonInit {
	override def daoSetup(): Unit = {
		Director.registerController(new IndexControler)
		Director.registerCustomPage(new ProgrammingPage)
		Director.registerCustomPage(new NetworkingPage)
		Director.registerCustomPage(new SmartHomePage)
	}

}