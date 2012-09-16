package dsl

import org.codehaus.groovy.control.CompilerConfiguration;


import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*

class DslScriptController {

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index() {
		redirect(action: "list", params: params)
	}

	def list() {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[surveyInstanceList: DslScript.list(params), surveyInstanceTotal: DslScript.count()]
	}

	def create() {
    def title = params.title
    def content = params.content
    def surveyInstance = new DslScript(params)
    surveyInstance.save(flush: true)
    render surveyInstance as JSON
  }

	def save() {
		def surveyInstance = new DslScript(params)
		if (!surveyInstance.save(flush: true)) {
			render(view: "create", model: [surveyInstance: surveyInstance])
			return
		}

		flash.message = message(code: 'default.created.message', args: [
			message(code: 'survey.label', default: 'DslScript'),
			surveyInstance.id
		])
		redirect(action: "show", id: surveyInstance.id)
	}

	def show() {
		def surveyInstance = DslScript.get(params.id)
		if (!surveyInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'survey.label', default: 'DslScript'),
				params.id
			])
			redirect(action: "list")
			return
		}

		[surveyInstance: surveyInstance]
	}

	def run() {
		println "in the inputs" + params
        def survey = DslScript.get(params.scriptId)
		def binding = new Binding()
		binding.setVariable("__inputs", params)
		def myBinding = binding.getVariable("__inputs")
        //println "binding answer map" + myBinding.answerMap
		if(params.answerMap){
			myBinding.answerMap = JSON.parse(params.answerMap)
		}
		println "answer map" + myBinding.answerMap
		def compilerConfig = new CompilerConfiguration()
		println "bindings before eval" + binding.__inputs
		compilerConfig.scriptBaseClass = DslScriptBaseScript.name
		compilerConfig.addCompilationCustomizers(new DslScriptCustomizer())

		def groovyShell = new GroovyShell(new GroovyClassLoader(), binding, compilerConfig)
		groovyShell.evaluate(survey.content)
		println "binding after eval" + binding.__inputs
		if(binding.__inputs.finished){
			//render(view:"finished",model:[inputs:binding.__inputs])
            render binding.__inputs as JSON
		}
		else {
			
			//render(view:"run",model:[inputs:binding.__inputs])
            render binding.__inputs as JSON
		}
	}

	def edit() {
		def surveyInstance = DslScript.get(params.id)
		if (!surveyInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'survey.label', default: 'DslScript'),
				params.id
			])
			redirect(action: "list")
			return
		}

		[surveyInstance: surveyInstance]
	}

	def update() {
		def surveyInstance = DslScript.get(params.id)
		if (!surveyInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'survey.label', default: 'DslScript'),
				params.id
			])
			redirect(action: "list")
			return
		}

		if (params.version) {
			def version = params.version.toLong()
			if (surveyInstance.version > version) {
				surveyInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
						[
							message(code: 'survey.label', default: 'DslScript')]
						as Object[],
						"Another user has updated this DslScript while you were editing")
				render(view: "edit", model: [surveyInstance: surveyInstance])
				return
			}
		}

		surveyInstance.properties = params

		if (!surveyInstance.save(flush: true)) {
			render(view: "edit", model: [surveyInstance: surveyInstance])
			return
		}

		flash.message = message(code: 'default.updated.message', args: [
			message(code: 'survey.label', default: 'DslScript'),
			surveyInstance.id
		])
		redirect(action: "show", id: surveyInstance.id)
	}

	def delete() {
		def surveyInstance = DslScript.get(params.id)
		if (!surveyInstance) {
			flash.message = message(code: 'default.not.found.message', args: [
				message(code: 'survey.label', default: 'DslScript'),
				params.id
			])
			redirect(action: "list")
			return
		}

		try {
			surveyInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [
				message(code: 'survey.label', default: 'DslScript'),
				params.id
			])
			redirect(action: "list")
		}
		catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [
				message(code: 'survey.label', default: 'DslScript'),
				params.id
			])
			redirect(action: "show", id: params.id)
		}
	}
}
