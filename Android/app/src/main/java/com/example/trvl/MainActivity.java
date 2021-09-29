package com.example.trvl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener {

    private AutoCompleteTextView autoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);

        // init autocompleteTextView
        ArrayAdapter<CharSequence> cAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setAdapter(cAdapter);
        autoCompleteTextView.setOnItemClickListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        this.autoCompleteTextView.setText("");
    }

    // inflate menu in ActionBar to add profile icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);

        MenuItem item = menu.findItem(R.id.actionbar_menu);

        // hide logout option
        menu.findItem(R.id.menu_action_logout).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    // triggered when profile icon is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_profile_icon) {
            Intent intent = new Intent(MainActivity.this, AccountHomeActivity.class);
            MainActivity.this.startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // triggered when dropdown item is selected
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        // fetch the user selected value
        String item = parent.getItemAtPosition(position).toString();

        Intent myIntent = new Intent(MainActivity.this, CountryActivity.class);
        myIntent.putExtra("country", item); //Optional parameters
        MainActivity.this.startActivity(myIntent);

    }
}