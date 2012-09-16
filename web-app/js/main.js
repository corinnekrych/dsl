window.onload = function() {

	var editor7 = ace.edit("editor7");
	editor7.setTheme("ace/theme/clouds");
	editor7.getSession().setMode("ace/mode/groovy");

	var commands7 = editor7.commands;

	commands7.addCommand({
		name : "save7",
		bindKey : {
			win : "Ctrl-S",
			mac : "Command-S",
			sender : "editor7"
		},
		exec : function() {
			var value = editor7.getSession().getValue();
			var title = $('#titleCreate').val();			
			submitCreateForm(title, value, "#output7");
		}
	});

    $('#runButton').bind('click',function() {
        var value = editor7.getSession().getValue();
        var title = $('#titleCreate').val();
        submitCreateForm(title, value, "#output7");
    });

}


function submitCreateForm(title, input, output) {
	var url = "http://localhost:8080/dsl/dslScript/create?=";
	//var url = "http://groovydsl.cloudfoundry.com/survey/create?=";
	$.post(url, {
		title:"myScript", content:input
	},function (data) {
		$("#displayQuestion").removeData();
		$('.displayAnswer').remove();
		$(".surveystart").show();
		$("#displayQuestion").data('scriptId',data.id);
		$("#displayQuestion").data('scriptContent',data.content);
		$('#scriptContent').text(data.content);
		$('#submitButton').click();
	});
}

$('#submitButton').bind('click', function() {
	var answer = $('#answer').val();
	$('#answer').val('');
	var answerMap = $("#displayQuestion").data('answerMap');
	var scriptId = $("#displayQuestion").data('scriptId');
	var counter = $("#displayQuestion").data('counter');
	var lastAssignement = $("#displayQuestion").data('lastAssignement');
	if (answerMap)
	  answerMap[lastAssignement] = answer;
	var stringAnswerMap = JSON.stringify(answerMap)
	var url = "http://localhost:8080/dsl/dslScript/run?=";
	//var url = "http://dsl.cloudfoundry.com/survey/run?=";
	
	$.post(url, {
		scriptId:scriptId, lastAssignement:lastAssignement, counter:counter, answer:answer, answerMap:stringAnswerMap
	}, function(data) {

		var answerMap = data.answerMap;
		var counter= data.counter;
		var lastAssignement = data.lastAssignement;
		$("#displayQuestion").data('answerMap',answerMap);
		$("#displayQuestion").data('counter',counter);
		$("#displayQuestion").data('lastAssignement',lastAssignement);
		if(data.finished==true) {
			var index = 1;
			$(".surveystart").hide();
			for (answer in answerMap) {
				var output7Value = '<div class="displayAnswer">For variable ' + answer +', answer is '+answerMap[answer] +'</div>';
			  $("#output7").append(output7Value);
			  index++;
			}
		} else {
			$("#displayQuestion").text(data.question);
		}
	});
});

