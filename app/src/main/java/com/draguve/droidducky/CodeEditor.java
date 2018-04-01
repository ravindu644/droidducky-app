package com.draguve.droidducky;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Arrays;

/**
 * Created by Draguve on 1/4/2018.
 */

public class CodeEditor extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private Integer EXECUTER_CODE = 1;
    private Integer DUCKYSCRIPT_EDIT = 0;
    private Integer COMMANDLINE_EDIT = 1;

    private Spinner langSpinner;
    private Spinner osSpinner;
    private static final String[] languages = {"be","br","ca","ch","de","dk","es","fi","fr","gb","hr","it","no","pt","ru","si","sv","tr","us"};
    private static final String[] os = {"Linux", "Windows", "Darwin","Windows-UAC"};
    private Script currentScript = null;
    private Script executerScript = null;
    private CommandLineScript currentCLScript = null;
    private CommandLineScript executerCLScript = null;
    ScriptsManager db;
    CommandLineManager commandLineDB;
    EditText codeTextBox = null;
    EditText scriptName = null;

    private Integer currentMode = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_code);
        Intent callingIntent = getIntent();
        String scriptID = callingIntent.getExtras().getString("idSelected",null);
        currentMode = callingIntent.getExtras().getInt("editingMode",0);

        //Spinner Settings
        langSpinner = (Spinner)findViewById(R.id.lang);
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,languages);

        osSpinner = (Spinner)findViewById(R.id.operating_system);
        if(currentMode == DUCKYSCRIPT_EDIT){
            osSpinner.setVisibility(View.GONE);
        }else{
            ArrayAdapter<String>osAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,os);
            osAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            osSpinner.setAdapter(osAdapter);
            osSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(adapter);
        langSpinner.setOnItemSelectedListener(this);

        codeTextBox = (EditText)findViewById(R.id.codeEdit);
        scriptName = (EditText)findViewById(R.id.scriptName);
        codeTextBox.setHorizontallyScrolling(true);
        codeTextBox.setHorizontalScrollBarEnabled(true);
        codeTextBox.setVerticalScrollBarEnabled(true);

        if(currentMode == DUCKYSCRIPT_EDIT){
            db = new ScriptsManager(this);
            if(scriptID!=null){
                currentScript = db.getScript(scriptID);
                if(currentScript!=null){
                    scriptName.setText(currentScript.getName());
                    codeTextBox.setText(currentScript.getCode());
                    //Can be optimized,the reverse search
                    langSpinner.setSelection(Arrays.asList(languages).indexOf(currentScript.getLang()));
                }else{
                    currentScript = new Script("","","us");
                    langSpinner.setSelection(18);
                }
            }else{
                currentScript = new Script("","","us");
                langSpinner.setSelection(18);
            }
        }else if(currentMode == COMMANDLINE_EDIT){
            commandLineDB = new CommandLineManager(this);
            if(scriptID!=null){
                currentCLScript = commandLineDB.getScript(scriptID);
                if(currentCLScript!=null){
                    scriptName.setText(currentCLScript.getName());
                    codeTextBox.setText(currentCLScript.getCode());
                    //Can be optimized,the reverse search
                    langSpinner.setSelection(Arrays.asList(languages).indexOf(currentScript.getLang()));
                    osSpinner.setSelection(currentCLScript.getOS().ordinal());
                }else{
                    currentCLScript = new CommandLineScript("","","us", CommandLineScript.OperatingSystem.WINDOWS);
                    langSpinner.setSelection(18);
                    osSpinner.setSelection(0);
                }
            }else{
                currentCLScript = new CommandLineScript("","","us", CommandLineScript.OperatingSystem.WINDOWS);
                langSpinner.setSelection(18);
                osSpinner.setSelection(0);
            }
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.code_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit DuckyScript");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        goBackToSelector();
        return true;
    }

    public void runCode(View view){
        if(currentMode == DUCKYSCRIPT_EDIT){
            executerScript = new Script("Temp",codeTextBox.getText().toString(),languages[langSpinner.getSelectedItemPosition()]);
            db.addScript(executerScript);
            final int result = 1;
            Intent codeExeIntent = new Intent(this,ExecuterActivity.class);
            codeExeIntent.putExtra("idSelected",executerScript.getID());
            this.startActivityForResult(codeExeIntent,result);
        }else if(currentMode == COMMANDLINE_EDIT){
            executerCLScript = new CommandLineScript("Temp",codeTextBox.getText().toString(),
                    languages[langSpinner.getSelectedItemPosition()], CommandLineScript.OperatingSystem.fromInteger(osSpinner.getSelectedItemPosition()));
            commandLineDB.addCommandScript(executerCLScript);
            //Have to change this for it to work
            final int result = 1;
            Intent codeExeIntent = new Intent(this,ExecuterActivity.class);
            codeExeIntent.putExtra("idSelected",executerScript.getID());
            this.startActivityForResult(codeExeIntent,result);
        }
    }

    public void saveScript(View view){
        if(currentMode == COMMANDLINE_EDIT){
            if(scriptName!=null) {
                if(scriptName.getText().length()==0){
                    Toast.makeText(this,"Please name the script to save it",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            currentCLScript.setCode(codeTextBox.getText().toString());
            currentCLScript.setName(scriptName.getText().toString());
            if(commandLineDB.getScript(currentCLScript.getID())!=null){
                new MaterialDialog.Builder(this)
                        .title("How do you want to save the script")
                        .positiveText("Create new")
                        .negativeText("Cancel")
                        .neutralText("Overwrite")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                commandLineDB.addCommandScript(new CommandLineScript(currentCLScript.getName(),currentCLScript.getCode(),currentCLScript.getLang(),currentCLScript.getOS()));
                                goBackToSelector();
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                commandLineDB.updateScript(currentCLScript);
                                goBackToSelector();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }else{
                commandLineDB.addCommandScript(currentCLScript);
                goBackToSelector();
            }
        }else if(currentMode == DUCKYSCRIPT_EDIT){
            if(scriptName!=null) {
                if(scriptName.getText().length()==0){
                    Toast.makeText(this,"Please name the script to save it",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            currentScript.setCode(codeTextBox.getText().toString());
            currentScript.setName(scriptName.getText().toString());
            if(db.getScript(currentScript.getID())!=null){
                new MaterialDialog.Builder(this)
                        .title("How do you want to save the script")
                        .positiveText("Create new")
                        .negativeText("Cancel")
                        .neutralText("Overwrite")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                db.addScript(new Script(currentScript.getName(),currentScript.getCode(),currentScript.getLang()));
                                goBackToSelector();
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                db.updateScript(currentScript);
                                goBackToSelector();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }else{
                db.addScript(currentScript);
                goBackToSelector();
            }
        }
    }

    public void goBackToSelector(){
        Intent goingBack = new Intent();
        setResult(RESULT_OK,goingBack);
        finish();
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        currentScript.setLang(languages[position]);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        currentScript.setLang("us");
        // Another interface callback
    }

    @Override
    public void onBackPressed() {
        if(currentMode == DUCKYSCRIPT_EDIT){
            if(!codeTextBox.getText().toString().trim().equals(currentScript.getCode().trim())){
                Toast.makeText(this,"Changes in script saved",Toast.LENGTH_SHORT).show();
                currentScript.setCode(codeTextBox.getText().toString());
                currentScript.setName(scriptName.getText().toString());
                db.updateScript(currentScript);
            }
        }else if(currentMode == COMMANDLINE_EDIT){
            if(!codeTextBox.getText().toString().trim().equals(currentCLScript.getCode().trim())){
                Toast.makeText(this,"Changes in script saved",Toast.LENGTH_SHORT).show();
                currentCLScript.setCode(codeTextBox.getText().toString());
                currentCLScript.setName(scriptName.getText().toString());
                commandLineDB.updateScript(currentCLScript);
            }
        }
        goBackToSelector();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXECUTER_CODE) {
            if(currentMode == DUCKYSCRIPT_EDIT){
                db.deleteScript(executerScript.getID());
                executerScript = null;
            }else if(currentMode == COMMANDLINE_EDIT){
                commandLineDB.deleteScript(executerCLScript.getID());
                executerCLScript = null;
            }
        }
    }
}
