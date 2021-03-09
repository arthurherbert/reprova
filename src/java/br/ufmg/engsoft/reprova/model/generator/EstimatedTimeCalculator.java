package br.ufmg.engsoft.reprova.model.generator;

import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;

public class EstimatedTimeCalculator extends ChainQuestionnaireGeneration{

  public Questionnaire generate(Questionnaire questionnaire){
	questionnaire.totalEstimatedTime = totalEstimatedTime(questionnaire);
	return handleGeneration(questionnaire);
  }

private int totalEstimatedTime(Questionnaire questionnaire) {
	int totalEstimatedTime = 0;
	for (Question question : questionnaire.questions) {
		totalEstimatedTime += question.estimatedTime;
	}
	return totalEstimatedTime;
}

}