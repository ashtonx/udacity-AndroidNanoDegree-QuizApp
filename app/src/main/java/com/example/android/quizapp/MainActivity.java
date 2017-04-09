package com.example.android.quizapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static android.util.Log.wtf;
import static com.example.android.quizapp.MainActivity.KanaType.HIRAGANA;
import static com.example.android.quizapp.MainActivity.KanaType.KATAKANA;
import static com.example.android.quizapp.MainActivity.KanaType.ROMAJI;
import static com.example.android.quizapp.MainActivity.QuestionType.FREE;
import static com.example.android.quizapp.MainActivity.QuestionType.MULTIPLE;
import static com.example.android.quizapp.MainActivity.QuestionType.SINGLE;


//4-10 questions
//question types:
// free text response.
// checkboxes (multiple  answers?)
// radio - one answer
// submit score.

public class MainActivity extends AppCompatActivity {
    //GLOBAL Types
    private final String FILE_NAME = "kana.xml";
    private final int NUMBER_OF_QUESTIONS = 10;
    private final int NUMBER_OF_MULTIPLE_ANSWERS = 4;
    List<Question> quizData = new ArrayList<>();

    public enum QuestionType {FREE, MULTIPLE, SINGLE}
    public enum KanaType {ROMAJI, HIRAGANA, KATAKANA}

    final int SUBMIT_ID = 42;
    LinearLayout layoutCheckbox;
    LinearLayout layoutRadio;
    LinearLayout layoutText;
    LinearLayout layoutScore;
    LinearLayout questionsLL;

    List<Kana> parsedData = null;
    Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutCheckbox = (LinearLayout) findViewById(R.id.layout_checkbox);
        layoutRadio = (LinearLayout) findViewById(R.id.layout_radio);
        layoutText = (LinearLayout) findViewById(R.id.layout_text_entry);
        layoutScore = (LinearLayout) findViewById(R.id.layout_score);
        questionsLL = (LinearLayout) findViewById(R.id.questions);
        start();
    }

    public void start() {
        parsedData = parseFile(FILE_NAME);
        quizData = generateQuiz(parsedData, NUMBER_OF_QUESTIONS,
                NUMBER_OF_MULTIPLE_ANSWERS);
        parsedData.clear();
        displayQuestions(quizData);
    }

    public void displayQuestions(List<Question> quizData) {
        ArrayList<View> questions = new ArrayList<>();
        for (int questionNumber = 0; questionNumber < quizData.size(); ++questionNumber) {
            questionsLL.addView(generateQuestionView(quizData.get(questionNumber),questionNumber));
        }
        View score= getLayoutInflater().inflate(R.layout.score, layoutScore, false);
        questionsLL.addView(score);
    }

    public void checkScore(View v){
        for(Question question:quizData){
            for (String input : question.user_input)
                wtf("checkScore", input);
        }
    }

//    private void countScore(){
//        int score = 0;
//        int maxQuestions = questionsLL.getChildCount();
//        View question=null;
//        EditText answerText= null;
//        CheckBox[] answersCheckbox = null;
//        RadioButton[] answerRadio = null;
//
//        for (int i = 0; i<maxQuestions; ++i) {
//            question = questionsLL.getChildAt(i);
//            switch (question.getId()) {
//                case R.id.layout_text_entry:
//                    answerText = (EditText) question.findViewById(R.id.answer);
//                    wtf("Count Score Text", answerText.getText().toString());
//                    break;
//                case R.id.layout_checkbox:
//                    answersCheckbox = new CheckBox[NUMBER_OF_MULTIPLE_ANSWERS];
//                    for (int ans = 0; ans < NUMBER_OF_MULTIPLE_ANSWERS; ++ans) {
//                        answersCheckbox[ans] = (CheckBox) question.findViewById(ans);
//                        wtf("Count Score Multiple", answersCheckbox[ans].toString());
//                    }
//                    break;
//                case R.id.layout_radio:
//                    answerRadio = new RadioButton[NUMBER_OF_MULTIPLE_ANSWERS];
//                    for (int ans = 0; ans < NUMBER_OF_MULTIPLE_ANSWERS; ++ans) {
//                        answerRadio[ans] = (RadioButton) question.findViewById(ans);
//                        wtf("Count Score Single", answerRadio[ans].toString());
//                    }
//                    break;
//                default:
//                    wtf("Count Score", "went past questions?");
//                    break;
//            }
//        }
//    }

    private List parseFile(String in) {
        List out = new ArrayList();
        XmlParser parser = new XmlParser();
        InputStream is;
        try {
            is = getAssets().open(in);
            out = parser.parse(is);
            is.close();
        } catch (IOException e) {
            Log.e("getData", e.getMessage());
        } catch (XmlPullParserException e) {
            Log.e("getData", e.getMessage());
        }
        return out;
    }

    public List generateQuiz(List<Kana> in, int numberOfQuestions, int numberOfAnswers) {
        List <Question> out = new ArrayList<>(numberOfQuestions);
        Collections.shuffle(in);
        QuestionType tmp_type;
        for (int i = 0; i < numberOfQuestions; ++i) {
            tmp_type = QuestionType.values()[rand.nextInt(QuestionType.values().length)];
            switch (tmp_type) {
                case FREE:
                    out.add(generateQuestion(in, i, FREE, numberOfAnswers));
                    break;
                case MULTIPLE:
                    out.add(generateQuestion(in, i, MULTIPLE, numberOfAnswers));
                    break;
                case SINGLE:
                    out.add(generateQuestion(in, i, SINGLE, numberOfAnswers));
                    break;
            }
        }
        return out;
    }

    private Question generateQuestion(List<Kana> in, int iter, QuestionType questionType, int numberOfAnswers) {
        Kana data = in.get(iter);
        String questionString;
        String questionCharacter;
        Vector<String> answers = null;
        KanaType answerType;
        Question out = null;
        switch (questionType) {
            case FREE:
                boolean questionHiragana = rand.nextBoolean();
                if (questionHiragana) questionCharacter = data.hiragana;
                else questionCharacter = data.katakana;
                questionString = "Type Romaji for " + questionCharacter + " :";
                out = new Question(data, questionString, FREE);
                break;

            case MULTIPLE:
                questionString = "Chose Hiragana and Katakana for " + data.romaji + " :";
                answers = new Vector<>(numberOfAnswers);
                answers.add(data.hiragana);
                answers.add(data.katakana);
                getRandomKana(in, answers, numberOfAnswers);
                Collections.shuffle(answers);
                out = new Question(data, questionString, MULTIPLE, answers);
                break;

            case SINGLE:
                answers = new Vector<>(numberOfAnswers);
                boolean questionRomaji = rand.nextBoolean();
                if (questionRomaji) {
                    boolean answerHiragana = rand.nextBoolean();
                    if (answerHiragana) {
                        answerType = HIRAGANA;
                        answers.add(data.hiragana);
                    } else {
                        answerType = KATAKANA;
                        answers.add(data.katakana);
                    }
                    questionCharacter = data.romaji;
                    getRandomKana(in, answers, numberOfAnswers);
                } else {
                    answerType = ROMAJI;
                    answers.add(data.romaji);
                    getRandomRomaji(in, answers, numberOfAnswers);
                    boolean QuestionHiragana = rand.nextBoolean();
                    if (QuestionHiragana) questionCharacter = data.hiragana;
                    else questionCharacter = data.katakana;
                }
                questionString = "Choose " + answerType.toString() + " for " + questionCharacter + " :";
                Collections.shuffle(answers);
                out = new Question(data, questionString, SINGLE, answers, answerType);
        }
        return out;
    }

    private void getRandomRomaji(List<Kana> in, Vector<String> answers, int numberOfAnswers) {
        while (answers.size() < numberOfAnswers)
            answers.add(in.get(rand.nextInt(in.size())).romaji);
    }

    private void getRandomKana(List<Kana> in, Vector<String> answers, int numberOfAnswers) {
        Boolean hiragana;
        while (answers.size() < numberOfAnswers) {
            hiragana = rand.nextBoolean();
            if (hiragana) {
                answers.add(in.get(rand.nextInt(in.size())).hiragana);
            } else {
                answers.add(in.get(rand.nextInt(in.size())).katakana);
            }
        }
    }

    public final class Question implements View.OnClickListener{
        public final Kana data;
        public final String question;
        public final QuestionType questionType;
        public final Vector<String> answers;
        public final KanaType answer_type;
        public ArrayList<String> user_input = new ArrayList<>();

        Question(Kana data, String question, QuestionType type) {
            //free question Constructor
            this.data = data;
            this.question = question;
            this.questionType = type;
            this.answers = null;
            this.answer_type = null;
        }

        Question(Kana data, String question, QuestionType questionType, Vector<String> answers) {
            this.data = data;
            this.question = question;
            this.questionType = questionType;
            this.answers = answers;
            this.answer_type = null;
        }

        Question(Kana data, String question, QuestionType questionType, Vector<String> answers,
                 KanaType answer_type) {
            this.data = data;
            this.question = question;
            this.questionType = questionType;
            this.answers = answers;
            this.answer_type = answer_type;
        }
        public void onClick(View v) {
            EditText inputText=null;
            CheckBox inputCheckBox=null;
            RadioButton inputRadio=null;

            switch(questionType){
                case FREE:
                    inputText = (EditText) v;
                    user_input.add(inputText.getText().toString());
                    break;
                case MULTIPLE:
                    inputCheckBox = (CheckBox) v;
                    user_input.add(inputCheckBox.getText().toString());
                    break;
                case SINGLE:
                    inputRadio = (RadioButton) v;
                    user_input.add(inputRadio.getText().toString());
            }
        }
    }
private View generateQuestionView (Question question, int questionNumber){
        View layout=null;
        TextView questionString;
    EditText answerText=null;
        CheckBox[] answersCheckbox = null;
        RadioButton[] answerRadio = null;
        switch (question.questionType) {
            case FREE:
                layout = getLayoutInflater().inflate(R.layout.question_text_entry, layoutText, false);
                questionString = (TextView) layout.findViewById(R.id.questionText);
                questionString.setText("(" + questionNumber + ") " + question.question);
                answerText = (EditText) layout.findViewById(R.id.answer);
                answerText.setOnClickListener(question);
                break;
            case MULTIPLE:
                layout = getLayoutInflater().inflate(R.layout.question_checkbox, layoutCheckbox, false);
                questionString = (TextView) layout.findViewById(R.id.questionCheckbox);
                questionString.setText("(" + questionNumber + ") " + question.question);
                answersCheckbox = new CheckBox[NUMBER_OF_MULTIPLE_ANSWERS];
                answersCheckbox[0] = (CheckBox) layout.findViewById(R.id.answer1);
                answersCheckbox[1] = (CheckBox) layout.findViewById(R.id.answer2);
                answersCheckbox[2] = (CheckBox) layout.findViewById(R.id.answer3);
                answersCheckbox[3] = (CheckBox) layout.findViewById(R.id.answer4);
                for (int i = 0; i < NUMBER_OF_MULTIPLE_ANSWERS; ++i) {
                    answersCheckbox[i].setText(question.answers.get(i));
                    answersCheckbox[i].setOnClickListener(question);
                }
                break;

            case SINGLE:
                layout = getLayoutInflater().inflate(R.layout.question_radio, layoutRadio, false);
                questionString = (TextView) layout.findViewById(R.id.questionRadio);
                questionString.setText("(" + (questionNumber + 1) + ") " + question.question);
                answerRadio = new RadioButton[NUMBER_OF_MULTIPLE_ANSWERS];
                answerRadio[0] = (RadioButton) layout.findViewById(R.id.answer1);
                answerRadio[1] = (RadioButton) layout.findViewById(R.id.answer2);
                answerRadio[2] = (RadioButton) layout.findViewById(R.id.answer3);
                answerRadio[3] = (RadioButton) layout.findViewById(R.id.answer4);
                //wonder if there's a way around it...
                for (int i = 0; i < NUMBER_OF_MULTIPLE_ANSWERS; ++i) {
                    answerRadio[i].setText(question.answers.get(i));
                    answerRadio[i].setOnClickListener(question);
                }
                break;
        }
        return layout;
    }
}


//TODO Get Answer
// view input, get answer(s).
//get answer to a question, send it for checks.
//TODO Check Answer
//compare answer(s) with questions ? find, compare.
//verify answer and send it to progress register
//TODO Register Progress
//Register which question it was
//Register wether it was correct or not.
//potentially register answer wether it was good or bad...
//TODO Check Progress
//check if done, if not generateQuiz new question if it's ok display Finished.
//TODO Finished
// Display Results, option to reset.
//TODO Clean and polish
//TODO Settings ?