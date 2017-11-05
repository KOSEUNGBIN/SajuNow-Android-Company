package com.test.landvibe.company.simplechat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.test.landvibe.company.R;


/**
 * Created by user on 2016-02-16.
 */
public class QuestionPageFragment extends Fragment {
    private int m_pageNumber;
    private String question;
    private ViewGroup m_rootView;
    private EditText editText;


    public static Fragment create(int iPageNumber, String question) {
        QuestionPageFragment fragment = new QuestionPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("page", iPageNumber);
        bundle.putString("question", question);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_pageNumber = getArguments().getInt("page");
        question = getArguments().getString("question");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_rootView = (ViewGroup) inflater.inflate(R.layout.question_page_fragment, container, false);
        TextView questionTextView = (TextView) m_rootView.findViewById(R.id.question_tv);
        editText = (EditText) m_rootView.findViewById(R.id.simple_prepare_et);


            if (m_pageNumber == 0) {
                questionTextView.setText(question);
            } else if (m_pageNumber == 1) {
                questionTextView.setText(question);
            } else if (m_pageNumber == 2) {
                questionTextView.setText(question);
            }


        return m_rootView;
    }

    public String getEditText() {
        return editText.getText().toString();
    }
}
