package cz.kamenitxan.web

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.service.EmailTemplateService
import cz.kamenitxan.jakon.core.task.{ResetPasswordRequestCleanerTask, TaskRunner}
import cz.kamenitxan.jakon.logging.LogCleanerTask
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.webui.entity.{ConfirmEmailEntity, ResetPasswordEmailEntity}
import cz.kamenitxan.web.controler.{IndexControler, NetworkingPage, ProgrammingPage, SmartHomePage}

import java.util.concurrent.TimeUnit
import scala.util.Using

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

	override protected def taskSetup(): Unit = {
		TaskRunner.registerTask(new LogCleanerTask)
		if (Settings.isEmailEnabled) {
			DBHelper.addDao(classOf[EmailEntity])
			DBHelper.addDao(classOf[EmailTemplateEntity])
			DBHelper.addDao(classOf[ConfirmEmailEntity])
			DBHelper.addDao(classOf[ResetPasswordEmailEntity])
			TaskRunner.registerTask(new EmailSendTask(1, TimeUnit.MINUTES))
			TaskRunner.registerTask(new ResetPasswordRequestCleanerTask)
		}
	}

	override protected def afterInit(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			val existingContactTemplate = EmailTemplateService.getByName("contact")
			if (existingContactTemplate == null) {
				val contactTemplate = new EmailTemplateEntity()
				contactTemplate.name = "contact"
				contactTemplate.from = "tomas@kamenitxan.eu"
				contactTemplate.subject = "Kontaktní formulář"
				contactTemplate.template = Using(scala.io.Source.fromFile("templates/email/contact.peb"))(_.mkString).get
				contactTemplate.create()
			}
		})
	}

}