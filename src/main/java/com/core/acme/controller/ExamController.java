package com.core.acme.controller;

import com.core.acme.DTO.QuestionDTO;
import com.core.acme.domain.Exam;
import com.core.acme.domain.Question;
import com.core.acme.domain.Student;
import com.core.acme.domain.enums.ExamStatus;
import com.core.acme.service.ExamService;


import lombok.SneakyThrows;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLOutput;
import java.util.List;

@RestController
@RequestMapping("/exam")
@CrossOrigin(origins = "*")
public class ExamController {

   // @Autowired
    @Autowired
    public ExamService examService; // should not be used creating a loop

    @PostMapping("/create_exam")
    public Exam createExam(@RequestBody Exam exam){// @RequestBody
        return examService.createExam(exam);

    }
    @PostMapping("/create-exam-from-form")
    public Exam createExamFromForm( Exam exam){// @RequestBody
        return examService.createExam(exam);

    }
    // start new exam is a different function, it sends the first question and starts the process
    @GetMapping("/start-exam")
    public ResponseEntity<QuestionDTO> startExam(String examId){ // @PathVariable
        examService.startExam(examId);
        return ResponseEntity.ok().body(examService.getFirstQuestion(examId));
    }

    @GetMapping("/update-exam")
    public void updateExam( String examId, String studentAns){

        if(examService.examEnded(examId)){
            //examHasEnded(examId); can not call another controller mapping method internally
            examService.setExamStatusCompleted(examId);
            return ;
        }
        examService.updateExam(examId,studentAns);
        System.out.println("ExamController : UpdateExam ");
    }
    @SneakyThrows
    @GetMapping("/get-next-question")
    public QuestionDTO getNextQuestion(String examId){
        if (examService.getExamByExamId(examId).getExamStatus() == ExamStatus.COMPLETED) {
            throw new Exception("Exam Ended, No More Questions Can Be Returned !");
        }
        return examService.getNextQuestion(examId);

    }
    @GetMapping("/check-exam-over")
    public boolean checkExamOver(String examId){
        if(examService.examEnded(examId)) {
            return true; // IMPLEMENT
        }
        return false;
    }
    @GetMapping("/return-exam-result")
    public String result(String examId){ // currently returning a string with score but will later be replaced by a dataTransferObject Result with values such as number of questions attempted , number of questions answered correctly , score evaluation of each question map etc.
        return "Exam Has Ended" +
                "You Have Scored"+examService.getExamByExamId((examId)).getScore(); // IMPLEMENT
    }

    @GetMapping("/reset-exam")
    public Exam resetExam( String examId){
        examService.resetExam(examId);
        return examService.getExamByExamId(examId);
    }
    @GetMapping("/find-exam-by-exam-id")
    public Exam getExamByExamId( String examId){
        return examService.getExamByExamId(examId);//find by exam id - user given
    }
    @GetMapping("/find-exam/{id}")
    public Exam getExamById(@PathVariable String id){
        return examService.getExamById(id);// find by database id
    }
    @GetMapping("/exam-list")
    public List<Exam> getAllExams(){
        return examService.getExamList();
    }
    @DeleteMapping("/delete-all-exams")
    public void deleteAllExams(){
        examService.deleteAllExam();
    }
    @DeleteMapping("delete-by-exam-id")
    public void deleteByExamId(String examId){
        examService.deleteByExamId(examId);
    }

    @GetMapping("/exam-ended")
    public String examHasEnded(String examId){
        return ("Exam Has Ended......." +
                "You have Scored"+examService.getExamByExamId(examId).getScore());
    }
}
