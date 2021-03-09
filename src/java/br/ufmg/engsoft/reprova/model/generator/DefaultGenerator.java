package br.ufmg.engsoft.reprova.model.generator;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;

import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.difficulty.DifficultyFactory;

public class DefaultGenerator implements IQuestionnaireGenerator{

  public Questionnaire generate(QuestionsDAO questionsDAO, String averageDifficulty, int questionsCount, int totalEstimatedTime){
    totalEstimatedTime = totalEstimatedTime(totalEstimatedTime);
	questionsCount = questionsCount(questionsCount);
	ArrayList<Question> questions = new ArrayList<Question>();
    ArrayList<Question> allQuestions = new ArrayList<Question>(questionsDAO.list(null, null));

    Collections.shuffle(allQuestions);
    questions(questionsCount, questions, allQuestions);
	return new Questionnaire.Builder()
                .averageDifficulty(averageDifficulty)
                .totalEstimatedTime(totalEstimatedTime)
                .questions(questions)
                .build();
  }

private int totalEstimatedTime(int totalEstimatedTime) {
	if (totalEstimatedTime == 0) {
		totalEstimatedTime = Questionnaire.DEFAULT_ESTIMATED_TIME_MINUTES;
	}
	return totalEstimatedTime;
}

private int questionsCount(int questionsCount) {
	if (questionsCount == 0) {
		questionsCount = Questionnaire.DEFAULT_QUESTIONS_COUNT;
	}
	return questionsCount;
}

private void questions(int questionsCount, ArrayList<Question> questions, ArrayList<Question> allQuestions) {
	for (int i = 0; i < questionsCount; i++) {
		if (i >= allQuestions.size()) {
			break;
		}
		questions.add(allQuestions.get(i));
	}
};
}