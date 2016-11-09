package com.onyx.android.edu.ui.chooseexercise;

import com.onyx.android.edu.EduApp;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.libedu.EduCloudManager;
import com.onyx.libedu.model.ChooseQuestionVariable;
import com.onyx.libedu.model.Difficult;
import com.onyx.libedu.model.KnowledgePoint;
import com.onyx.libedu.model.QuestionType;
import com.onyx.libedu.model.Stage;
import com.onyx.libedu.model.Subject;
import com.onyx.libedu.model.Textbook;
import com.onyx.libedu.model.Version;
import com.onyx.libedu.request.cloud.GetBookNodesRequest;
import com.onyx.libedu.request.cloud.GetKnowledgePointsRequest;
import com.onyx.libedu.request.cloud.GetQuestionTypeRequest;
import com.onyx.libedu.request.cloud.GetSubjectRequest;
import com.onyx.libedu.request.cloud.GetTextBooksRequest;
import com.onyx.libedu.request.cloud.GetVersionsRequest;

import java.util.List;

/**
 * Created by ming on 16/6/28.
 */
public class ChooseExercisePresenter implements ChooseExerciseContract.ChooseExercisePresenter {

    private ChooseExerciseContract.ChooseExerciseView chooseExerciseView;
    private EduCloudManager eduCloudManager;
    private ChooseQuestionVariable chooseQuestionVariable;

    public ChooseExercisePresenter(ChooseExerciseContract.ChooseExerciseView chooseExerciseView){
        this.chooseExerciseView = chooseExerciseView;
        chooseExerciseView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        eduCloudManager = new EduCloudManager();
        chooseQuestionVariable = new ChooseQuestionVariable();
    }

    @Override
    public void unSubscribe() {

    }

    @Override
    public void loadSubjects(Stage stage) {
        chooseQuestionVariable.setStage(stage);
        final GetSubjectRequest subjectRequest = new GetSubjectRequest(stage.getId());
        eduCloudManager.submitRequest(EduApp.instance(), subjectRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                List<Subject> subjects = subjectRequest.getSubjects();
                chooseExerciseView.showSubjects(subjects);
            }
        });
    }

    @Override
    public void loadVersions(Subject subject) {
        chooseQuestionVariable.setSubject(subject);
        final GetVersionsRequest versionsRequest = new GetVersionsRequest(subject.getId());
        eduCloudManager.submitRequest(EduApp.instance(), versionsRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                chooseExerciseView.showVersions(versionsRequest.getVersions());
            }
        });
    }

    @Override
    public void loadTextbooks(Version version) {
        chooseQuestionVariable.setVersion(version);
        final GetTextBooksRequest textBooksRequest = new GetTextBooksRequest(version.getId());
        eduCloudManager.submitRequest(EduApp.instance(), textBooksRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                chooseExerciseView.showTextbooks(textBooksRequest.getTextbooks());
            }
        });
    }

    @Override
    public void loadBookNodes() {
        if (chooseQuestionVariable.getTextbook() == null) {
            return;
        }
        final GetBookNodesRequest bookNodesRequest = new GetBookNodesRequest(chooseQuestionVariable.getTextbook().getId());
        eduCloudManager.submitRequest(EduApp.instance(), bookNodesRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                chooseExerciseView.showBookNodes(bookNodesRequest.getBookNodes());
            }
        });
    }

    @Override
    public void loadKnowledgePoints() {
        if (chooseQuestionVariable.getSubject() == null) {
            return;
        }
        final GetKnowledgePointsRequest knowledgePointsRequest = new GetKnowledgePointsRequest(chooseQuestionVariable.getSubject().getId());
        eduCloudManager.submitRequest(EduApp.instance(), knowledgePointsRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                List<KnowledgePoint> knowledgePoints = knowledgePointsRequest.getKnowledgePoints();
                chooseExerciseView.showKnowledgePoints(knowledgePoints);
            }
        });
    }

    @Override
    public void loadQuestionType(Subject subject) {
        final GetQuestionTypeRequest questionRequest = new GetQuestionTypeRequest(subject.getId());
        eduCloudManager.submitRequest(EduApp.instance(), questionRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                chooseExerciseView.showQuestionType(questionRequest.getQuestionTypes());
            }
        });
    }

    @Override
    public ChooseQuestionVariable getChooseQuestionVariable() {
        return chooseQuestionVariable;
    }

    @Override
    public void chooseTextbook(Textbook textbook) {
        chooseQuestionVariable.setTextbook(textbook);
    }

    @Override
    public void chooseQuestionType(QuestionType questionType) {
        chooseQuestionVariable.setQuestionType(questionType);
    }

    @Override
    public void chooseDifficult(Difficult difficult) {
        chooseQuestionVariable.setDifficult(difficult);
    }
}
