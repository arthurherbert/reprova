package br.ufmg.engsoft.reprova.model;


import java.util.Map;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class QuestionCalculations {
	public double calculateGradeAverage(Map<Semester, Map<String, Map<String, Float>>> thisRecord) {
		double acc = acc(thisRecord);
		return acc / thisRecord.size();
	}

	public double acc(Map<Semester, Map<String, Map<String, Float>>> thisRecord) {
		double acc = 0;
		for (Map.Entry<Semester, Map<String, Map<String, Float>>> entry : thisRecord.entrySet()) {
			double acc2 = acc2(entry);
			acc += acc2 / entry.getValue().entrySet().size();
		}
		return acc;
	}

	public double sum(Map<Semester, Map<String, Map<String, Float>>> thisRecord) {
		double average = this.calculateGradeAverage(thisRecord);
		double sum = 0.0;
		for (Map.Entry<Semester, Map<String, Map<String, Float>>> entry : thisRecord.entrySet()) {
			for (Map.Entry<String, Map<String, Float>> innerEntry : entry.getValue().entrySet()) {
				for (var notas : innerEntry.getValue().values()) {
					sum += Math.pow(notas - average, 2);
				}
			}
		}
		return sum;
	}

	public double acc2(Map.Entry<Semester, Map<String, Map<String, Float>>> entry) {
		double acc2 = 0;
		for (Map.Entry<String, Map<String, Float>> innerEntry : entry.getValue().entrySet()) {
			acc2 += innerEntry.getValue().values().stream().mapToDouble(Float::doubleValue).average().orElse(0);
		}
		return acc2;
	}

	public int qtdNotas(Map<Semester, Map<String, Map<String, Float>>> thisRecord) {
		int qtdNotas = 0;
		for (Map.Entry<Semester, Map<String, Map<String, Float>>> entry : thisRecord.entrySet()) {
			for (Map.Entry<String, Map<String, Float>> innerEntry : entry.getValue().entrySet()) {
				for (var notas : innerEntry.getValue().values()) {
					qtdNotas++;
				}
			}
		}
		return qtdNotas;
	}

	public double calculateGradeMedian(Map<Semester, Map<String, Map<String, Float>>> thisRecord) {
		List<Float> gradeList = new ArrayList<Float>();
		for (Map.Entry<Semester, Map<String, Map<String, Float>>> entry : thisRecord.entrySet()) {
			for (Map.Entry<String, Map<String, Float>> innerEntry : entry.getValue().entrySet()) {
				for (var notas : innerEntry.getValue().values()) {
					gradeList.add(notas);
				}
			}
		}
		Collections.sort(gradeList);
		if (gradeList.size() == 0) {
			return 0.0;
		}
		int i = gradeList.size() / 2;
		if (gradeList.size() % 2 == 0) {
			return (gradeList.get(i - 1) + gradeList.get(i)) / 2;
		} else {
			return gradeList.get(i);
		}
	}

	public double calculateGradeStandardDeviation(Map<Semester, Map<String, Map<String, Float>>> thisRecord) {
		double sum = sum(thisRecord);
		int qtdNotas = qtdNotas(thisRecord);
		double stdDev = Math.sqrt(sum / (qtdNotas - 1));
		return stdDev;
	}
}