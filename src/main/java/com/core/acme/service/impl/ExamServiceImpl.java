package com.core.acme.service.impl;

import com.core.acme.DTO.QuestionDTO;
import com.core.acme.domain.Exam;
import com.core.acme.domain.Question;
import com.core.acme.domain.Test;
import com.core.acme.repository.ExamRepository;
import com.core.acme.service.AcmeService;
import com.core.acme.service.ExamService;
import com.core.acme.service.TestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.core.acme.domain.enums.ExamStatus;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private final ExamRepository examRepository;
    @Autowired
    private final AcmeService acmeService;
    @Autowired
    private final TestService testService;
    ExamServiceImpl(ExamRepository examRepository,AcmeService acmeService,TestService testService){
        this.examRepository = examRepository;
        this.acmeService = acmeService;
        this.testService = testService;
    }

    @Override
    public Exam createExam(Exam exam) {
        exam.setExamStatus(ExamStatus.NOT_YET_STARTED);
        exam.setQuestionNumber(0);
        exam.setScore(0);
        return examRepository.save(exam);
    }

    @Override
    public String startExam(String examId) {       // starts new exam by returning the exam id
        Exam exam = getExamByExamId(examId);
        exam.setExamStatus(ExamStatus.ONGOING);
        examRepository.save(exam);
        //getFirstQuestion(exam.getExamId());
        return null;
    }

    @Override                                               // Sequence 1
    public void updateExam(String examId, String studentAns){              // updates everytime student sends an answer......
        // to update exam variables and update scores.  the index of current questions are updated by the checkAnswer Function.
        Exam exam = examRepository.findByExamId(examId);
        Test test = testService.getTestByTestId(exam.getTestId());
        List<List<String>> questionTree = test.getQuestionsInTest();
        Question currentQuestion = acmeService.getQuestionByQuestionId(test.getQuestionsInTest().get(exam.getCurrentQuestionIndex()).get(exam.getCurrentSubQuestionIndex()));
        exam.setQuestionNumber(exam.getQuestionNumber()+1);
        if(exam.getQuestionNumber() == test.getQuestionsToBeAttempted())
        {
            // end exam
            exam.setExamStatus(ExamStatus.COMPLETED);
            examRepository.save(exam);
            displayResults(examId);
            return ;
        }
        else {
            if (currentQuestion.getCorrectOpt().equals(studentAns)) {
                if (exam.getCurrentSubQuestionIndex() < questionTree.get(exam.getCurrentQuestionIndex()).size() - 1)//questionTree.get(exam.getCurrentQuestionIndex()).listIterator().hasNext())
                {//exam.getCurrentSubQuestionIndex() < questionTree.get(exam.getCurrentQuestionIndex()).size() - 1) {
                    exam.setCurrentSubQuestionIndex(exam.getCurrentSubQuestionIndex() + 1);
                    System.out.println("Q:" + exam.getCurrentQuestionIndex() + "subQ:" + exam.getCurrentSubQuestionIndex());
                    exam.setScore(exam.getScore() + 4);
                    System.out.println("correct Ans | score : " + exam.getScore());
                } else if (exam.getCurrentQuestionIndex() < questionTree.size() - 1)//questionTree.listIterator().hasNext())
                {    // if [next Question exists]
                    exam.setCurrentSubQuestionIndex(0);
                    exam.setCurrentQuestionIndex(exam.getCurrentQuestionIndex() + 1);
                    System.out.println("Q:" + exam.getCurrentQuestionIndex() + "subQ:" + exam.getCurrentSubQuestionIndex());
                    exam.setScore(exam.getScore() + 4);
                    System.out.println("correct Ans | score : " + exam.getScore());
                } else {
                    // We are on last subQuestion of last Question - End Exam and Display Results.
                    exam.setExamStatus(ExamStatus.COMPLETED);
                    examRepository.save(exam);
                    displayResults(examId);
                }
                System.out.println("Exam-saved-correct ans" + examRepository.save(exam));

            } else {
                if (exam.getCurrentQuestionIndex() < questionTree.size() - 1)//questionTree.listIterator().hasNext())
                {  // incorrect answer given , go to next question skipping all current sub-question of Question of [next Question exists]
                    exam.setCurrentSubQuestionIndex(0);
                    exam.setCurrentQuestionIndex(exam.getCurrentQuestionIndex() + 1);
                    System.out.println("Q:" + exam.getCurrentQuestionIndex() + "subQ:" + exam.getCurrentSubQuestionIndex());
                    exam.setScore(exam.getScore() - 1);
                    System.out.println("Incorrect Ans | score : " + exam.getScore());
                    System.out.println(examRepository.save(exam));
                } else {
                    // We are on last Question no more questions exist, and we have given incorrect answer.
                    // End Exam and Display Results.
                    exam.setExamStatus(ExamStatus.COMPLETED);
                    examRepository.save(exam);
                    displayResults(examId);
                }

            }
        }
        // don't use statements after this


    } // do it  with only exam ID

//    @Override                                               // Sequence 2
//    public boolean checkAnswer(String examId, String studentAns) {
//        Exam exam = examRepository.findByExamId(examId);
//        Test test = testService.getTestByTestId(exam.getTestId());
//        List<List<String>> questionTree = test.getQuestionsInTest();
//        Question currentQuestion = acmeService.getQuestionByQuestionId(test.getQuestionsInTest().get(exam.getCurrentQuestionIndex()).get(exam.getCurrentSubQuestionIndex()));
//
//        if (currentQuestion.getCorrectOpt().equals(studentAns)) {
//            if (questionTree.get(exam.getCurrentQuestionIndex()).listIterator().hasNext()){//exam.getCurrentSubQuestionIndex() < questionTree.get(exam.getCurrentQuestionIndex()).size() - 1) {
//                exam.setCurrentSubQuestionIndex(exam.getCurrentSubQuestionIndex() + 1);
//                System.out.println("Q:" + exam.getCurrentQuestionIndex() + "subQ:" + exam.getCurrentSubQuestionIndex());
//
//            } else if (questionTree.listIterator().hasNext()) {    // if [next Question exists]
//                exam.setCurrentSubQuestionIndex(0);
//                exam.setCurrentQuestionIndex(exam.getCurrentQuestionIndex() + 1);
//                System.out.println("Q:" + exam.getCurrentQuestionIndex() + "subQ:" + exam.getCurrentSubQuestionIndex());
//
//            } else {
//                // we have answered incorrectly and no more questions left now, so EXAM ENDED
//                isFinished=true;
//                displayResults(examId);
//            }
//            System.out.println("Exam-saved-correct ans"+examRepository.save(exam));
//            return true;
//        }else{
//            if (questionTree.listIterator().hasNext()) {  // incorrect answer given , go to next question skipping all current sub-question of Question of [next Question exists]
//                exam.setCurrentSubQuestionIndex(0);
//                exam.setCurrentQuestionIndex(exam.getCurrentQuestionIndex() + 1);
//                System.out.println("Q:" + exam.getCurrentQuestionIndex() + "subQ:" + exam.getCurrentSubQuestionIndex());
//                System.out.println(examRepository.save(exam));
//            } else {
//                // We are on last Question no more questions exist, and we have given incorrect answer.
//                // End Exam and Display Results.
//                isFinished= true;
//                displayResults(examId); // only for console
//            }
//            return false;
//        }
//    }

    @Override
    public void resetExam(String examId)
    {
        Exam existingExam = getExamByExamId(examId);
        existingExam.setScore(0);
        existingExam.setCurrentQuestionIndex(0);
        existingExam.setCurrentSubQuestionIndex(0);
        existingExam.setQuestionNumber(0);
        existingExam.setExamStatus(ExamStatus.NOT_YET_STARTED);
        examRepository.save(existingExam);
    }
    @Override
    public void endExam(String examId){
        Exam exam = getExamByExamId(examId);
        exam.setExamStatus(ExamStatus.COMPLETED);
        examRepository.save(exam);
    }
    @Override
    public void setExamStatusCompleted(String examId){
        Exam exam = getExamByExamId(examId);
        exam.setExamStatus(ExamStatus.COMPLETED);
        examRepository.save(exam);
    }
    @Override
    public boolean examEnded(String examId) {
        if(getExamByExamId(examId).getExamStatus() == ExamStatus.COMPLETED){
            return  true;
        }
        return false;
    }

    @Override
    public QuestionDTO getFirstQuestion(String examId){
        Exam exam = getExamByExamId(examId);
        String testId = exam.getTestId();
        Test test = testService.getTestByTestId(testId);
        String firstQuestionId = test.getQuestionsInTest().get(exam.getCurrentQuestionIndex()).get(exam.getCurrentSubQuestionIndex());
        System.out.println("first question : "+firstQuestionId);
        return acmeService.convertQuestionToDTO(acmeService.getQuestionByQuestionId(firstQuestionId));
    }

    @Override
    public QuestionDTO getNextQuestion(String examId){
        //if(isCorrect) correct and incorrect logic has been stated in the check answer and next question index has been decided in checkAnswer method
        // we just have to return the next question using the exam.currentQuestionIndex and currentSubQuestionIndex
        Exam exam = examRepository.findByExamId(examId);
        String testId = exam.getTestId();
        Test test = testService.getTestByTestId(testId);
        String nextQuestionId = test.getQuestionsInTest().get(exam.getCurrentQuestionIndex()).get(exam.getCurrentSubQuestionIndex());
        System.out.println("Next Question called : "+nextQuestionId);
        return acmeService.convertQuestionToDTO(acmeService.getQuestionByQuestionId(nextQuestionId));
    }
    
    
    public String displayResults(String examId){                       // Auto-sequence 3
        Exam exam = examRepository.findByExamId(examId);
        System.out.println("The Exam Has Ended , You Have scored : "+exam.getScore());
        return "The Exam Has Ended , You Have scored : "+exam.getScore();
    }
    @Override
    public Exam getExamById(String id) {
        Optional<Exam> examOptionalObject = examRepository.findById(id);
        return examOptionalObject.orElse(null);
    } // by database id

    @Override
    public List<Exam> getExamList() {
        return examRepository.findAll();
    }

    @Override
    public Exam getExamByExamId(String examId){
        return examRepository.findByExamId(examId);
    }

    @Override
    public void deleteAllExam() {
        examRepository.deleteAll();
    }

    @Override
    public void deleteByExamId(String examId) {
        examRepository.deleteByExamId(examId);
    }
        
}

/*
* getFirstQuestion:
*       CheckAnswer :
*           UpdateScore & ReturnNextQuestion Accordingly:
*                   CheckAnswer...............
* */
