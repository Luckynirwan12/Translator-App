package com.example.translation_app;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText inputText;
    private TextView outputText;
    private Button translateBtn, speakBtn;
    private Spinner sourceSpinner, targetSpinner;
    private Translator translator;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        translateBtn = findViewById(R.id.translateBtn);
        speakBtn = findViewById(R.id.speakBtn);
        sourceSpinner = findViewById(R.id.sourceLangSpinner);
        targetSpinner = findViewById(R.id.targetLangSpinner);

        tts = new TextToSpeech(this, this);

        Map<String, String> languages = new HashMap<>();
        languages.put("English", TranslateLanguage.ENGLISH);
        languages.put("Hindi", TranslateLanguage.HINDI);
        languages.put("Spanish", TranslateLanguage.SPANISH);
        languages.put("French", TranslateLanguage.FRENCH);
        languages.put("German", TranslateLanguage.GERMAN);
        languages.put("Chinese", TranslateLanguage.CHINESE);
        languages.put("Japanese", TranslateLanguage.JAPANESE);
        languages.put("Korean", TranslateLanguage.KOREAN);
        languages.put("Russian", TranslateLanguage.RUSSIAN);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languages.keySet().toArray(new String[0])
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(adapter);
        targetSpinner.setAdapter(adapter);

        sourceSpinner.setSelection(0);
        targetSpinner.setSelection(1);

        translateBtn.setOnClickListener(v -> {

            String text = inputText.getText().toString();

            if (text.isEmpty()) {
                Toast.makeText(this, "Enter text", Toast.LENGTH_SHORT).show();
                return;
            }

            String sourceLang = languages.get(sourceSpinner.getSelectedItem().toString());
            String targetLang = languages.get(targetSpinner.getSelectedItem().toString());

            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(sourceLang)
                    .setTargetLanguage(targetLang)
                    .build();

            translator = Translation.getClient(options);

            outputText.setText("Translating...");

            translator.downloadModelIfNeeded()
                    .addOnSuccessListener(unused ->
                            translator.translate(text)
                                    .addOnSuccessListener(translated ->
                                            outputText.setText(translated)
                                    )
                                    .addOnFailureListener(e ->
                                            outputText.setText("Translation failed")
                                    )
                    )
                    .addOnFailureListener(e ->
                            outputText.setText("Model download failed")
                    );
        });

        speakBtn.setOnClickListener(v -> {
            String textToSpeak = outputText.getText().toString();
            if (!textToSpeak.isEmpty()) {
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Toast.makeText(this, "No translated text", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (translator != null) translator.close();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
