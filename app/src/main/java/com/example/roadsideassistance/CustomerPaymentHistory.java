package com.example.roadsideassistance;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerPaymentHistory extends AppCompatActivity {
    Customer customer;
    AppDatabase database;
    public Spinner months;
    public Spinner years;
    ArrayAdapter<CharSequence> monthsAdapter;
    ArrayAdapter<String> yearsAdapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_payment_history);

        database = AppDatabase.getDatabase(this);
        customer = getIntent().getParcelableExtra("Customer");

        months = findViewById(R.id.paymentHistoryMonth);
        years = findViewById(R.id.paymentHistoryYear);
        monthsAdapter = ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_dropdown_item);
        Date earliestServiceDate = database.serviceDao().getEarliestFinishedServiceCustomer(customer.username);
        Date earliestSubDate = database.customerDao().getEarliestSubPayment(customer.username);
        Date earliestDate = earliestServiceDate.before(earliestSubDate) ? earliestServiceDate : earliestSubDate;
        if(!earliestDate.equals(new Date(0))) {
            //Toast.makeText(this, "Equals Date(0)", Toast.LENGTH_LONG).show();
            //Toast.makeText(this, "" + earliestDate.toString(), Toast.LENGTH_LONG).show();
            Date currDate = new Date();
            ArrayList<String> yearsInString = new ArrayList<>();
            if (earliestDate != null && currDate.getYear() != earliestDate.getYear()) {
                yearsInString.add("" + (currDate.getYear() + 1900));
                for (int i = currDate.getYear() - 1; i >= earliestDate.getYear(); i--) {
                    yearsInString.add("" + (i + 1900));
                }
            } else {
                yearsInString.add("" + (currDate.getYear() + 1900));
            }
            yearsAdapters = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearsInString);

            months.setAdapter(monthsAdapter);
            years.setAdapter(yearsAdapters);

            months.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    fillLayout(position, Integer.parseInt((String) years.getSelectedItem()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            years.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    fillLayout(months.getSelectedItemPosition(), Integer.parseInt(years.getItemAtPosition(position).toString()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            View costLabel = findViewById(R.id.payLabel);
            costLabel.post(new Runnable() {
                @Override
                public void run() {
                    Date currDate = new Date();
                    fillLayout(currDate.getMonth(), currDate.getYear());
                }
            });
        }
        else {
            LinearLayout serviceListLayout = findViewById(R.id.paymentHistoryLayout);
            TextView noHistoryText = new TextView(this);
            noHistoryText.setText("No Payment History");
            serviceListLayout.addView(noHistoryText);
            TextView totalText = findViewById(R.id.paymentHistoryTotal);
            totalText.setVisibility(View.INVISIBLE);
            months.setVisibility(View.INVISIBLE);
            years.setVisibility(View.INVISIBLE);
        }
    }

    private void fillLayout(int month, int year) {
        Date firstOfMonth = new Date(year-1900, month, 1);
        Date lastOfMonth = new Date(year-1900, month, 31);
        List<Service> servicesInMonth = database.serviceDao().getServicesInMonthCustomer(customer.username, firstOfMonth, lastOfMonth);
        List<SubscriptionPayment> subPaymentsInMonth = database.customerDao().getSubPaymentsInMonth(customer.username, firstOfMonth, lastOfMonth);

        float total = 0;

        LinearLayout serviceListLayout = findViewById(R.id.paymentHistoryLayout);
        serviceListLayout.removeViews(0, serviceListLayout.getChildCount());
        if((servicesInMonth != null && servicesInMonth.size() > 0) || (subPaymentsInMonth != null && subPaymentsInMonth.size() > 0)) {
            //Toast.makeText(this, "here", Toast.LENGTH_LONG).show();
            int servicesIndex = 0;
            int subsIndex = 0;
            for(int i = 0; i < (servicesInMonth.size() + subPaymentsInMonth.size()); i++) {
                LinearLayout outerLayout = new LinearLayout(this);
                outerLayout.setOrientation(LinearLayout.HORIZONTAL);
                outerLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                if(servicesIndex >= servicesInMonth.size()) {
                    //fill out rest of layout with sub payments
                    total += subPaymentsInMonth.get(subsIndex).amount;
                    TextView typeText = new TextView(this);
                    typeText.setText("Sub");
                    typeText.setWidth(findViewById(R.id.typeLabel).getWidth());
                    typeText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    typeText.setPadding(5,5,5,5);
                    typeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(typeText);

                    TextView dateText = new TextView(this);
                    dateText.setText(String.format("%d/%d/%d",
                            subPaymentsInMonth.get(subsIndex).time.getDate(),
                            subPaymentsInMonth.get(subsIndex).time.getMonth() + 1,
                            subPaymentsInMonth.get(subsIndex).time.getYear() + 1900));
                    dateText.setWidth(findViewById(R.id.dateLabel).getWidth());
                    dateText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    dateText.setPadding(5,5,5,5);
                    dateText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(dateText);

                    TextView costText = new TextView(this);
                    costText.setText(String.format("$%.2f", subPaymentsInMonth.get(subsIndex).amount));
                    costText.setWidth(findViewById(R.id.payLabel).getWidth());
                    costText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    costText.setPadding(5,5,5,5);
                    costText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(costText);

                    /*
                    TextView sub = new TextView(this);
                    sub.setText(String.format("Sub Date: %d/%d/%d Cost: $%.2f",
                            subPaymentsInMonth.get(subsIndex).time.getDate(),
                            subPaymentsInMonth.get(subsIndex).time.getMonth(),
                            subPaymentsInMonth.get(subsIndex).time.getYear() + 1900,
                            subPaymentsInMonth.get(subsIndex).amount));
                    serviceListLayout.addView(sub);
                    */
                    subsIndex++;
                }
                else if(subsIndex >= subPaymentsInMonth.size()) {
                    //fill out res of layout with service payments
                    total += servicesInMonth.get(servicesIndex).cost;
                    TextView typeText = new TextView(this);
                    typeText.setText("Service");
                    typeText.setWidth(findViewById(R.id.typeLabel).getWidth());
                    typeText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    typeText.setPadding(5,5,5,5);
                    typeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(typeText);

                    TextView dateText = new TextView(this);
                    dateText.setText(String.format("%d/%d/%d",
                            servicesInMonth.get(servicesIndex).time.getDate(),
                            servicesInMonth.get(servicesIndex).time.getMonth() + 1,
                            servicesInMonth.get(servicesIndex).time.getYear() + 1900));
                    dateText.setWidth(findViewById(R.id.dateLabel).getWidth());
                    dateText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    dateText.setPadding(5,5,5,5);
                    dateText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(dateText);

                    TextView costText = new TextView(this);
                    costText.setText(String.format("$%.2f", servicesInMonth.get(servicesIndex).cost));
                    costText.setWidth(findViewById(R.id.payLabel).getWidth());
                    costText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    costText.setPadding(5,5,5,5);
                    costText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(costText);

                    /*
                    TextView service = new TextView(this);
                    service.setText(String.format("Service Date: %d/%d/%d  Cost: $%.2f",
                            servicesInMonth.get(servicesIndex).time.getDate(),
                            servicesInMonth.get(servicesIndex).time.getMonth(),
                            servicesInMonth.get(servicesIndex).time.getYear() + 1900,
                            servicesInMonth.get(servicesIndex).cost));
                    serviceListLayout.addView(service);
                    */
                    servicesIndex++;
                }
                else if(servicesInMonth.get(servicesIndex).time.after(subPaymentsInMonth.get(subsIndex).time)) {
                    total += servicesInMonth.get(servicesIndex).cost;
                    TextView typeText = new TextView(this);
                    typeText.setText("Service");
                    typeText.setWidth(findViewById(R.id.typeLabel).getWidth());
                    typeText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    typeText.setPadding(5,5,5,5);
                    typeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(typeText);

                    TextView dateText = new TextView(this);
                    dateText.setText(String.format("%d/%d/%d",
                            servicesInMonth.get(servicesIndex).time.getDate(),
                            servicesInMonth.get(servicesIndex).time.getMonth() + 1,
                            servicesInMonth.get(servicesIndex).time.getYear() + 1900));
                    dateText.setWidth(findViewById(R.id.dateLabel).getWidth());
                    dateText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    dateText.setPadding(5,5,5,5);
                    dateText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(dateText);

                    TextView costText = new TextView(this);
                    costText.setText(String.format("$%.2f", servicesInMonth.get(servicesIndex).cost));
                    costText.setWidth(findViewById(R.id.payLabel).getWidth());
                    costText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    costText.setPadding(5,5,5,5);
                    costText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(costText);

                    /*
                    TextView service = new TextView(this);
                    service.setText(String.format("Service Date: %d/%d/%d  Cost: $%.2f",
                            servicesInMonth.get(servicesIndex).time.getDate(),
                            servicesInMonth.get(servicesIndex).time.getMonth(),
                            servicesInMonth.get(servicesIndex).time.getYear() + 1900,
                            servicesInMonth.get(servicesIndex).cost));
                    serviceListLayout.addView(service);
                    */
                    servicesIndex++;
                }
                else {
                    total += subPaymentsInMonth.get(subsIndex).amount;
                    TextView typeText = new TextView(this);
                    typeText.setText("Sub");
                    typeText.setWidth(findViewById(R.id.typeLabel).getWidth());
                    typeText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    typeText.setPadding(5,5,5,5);
                    typeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(typeText);

                    TextView dateText = new TextView(this);
                    dateText.setText(String.format("%d/%d/%d",
                            subPaymentsInMonth.get(subsIndex).time.getDate(),
                            subPaymentsInMonth.get(subsIndex).time.getMonth() + 1,
                            subPaymentsInMonth.get(subsIndex).time.getYear() + 1900));
                    dateText.setWidth(findViewById(R.id.dateLabel).getWidth());
                    dateText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    dateText.setPadding(5,5,5,5);
                    dateText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(dateText);

                    TextView costText = new TextView(this);
                    costText.setText(String.format("$%.2f", subPaymentsInMonth.get(subsIndex).amount));
                    costText.setWidth(findViewById(R.id.payLabel).getWidth());
                    costText.setBackground(getResources().getDrawable(R.drawable.border_sharp));
                    costText.setPadding(5,5,5,5);
                    costText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    outerLayout.addView(costText);

                    /*
                    TextView sub = new TextView(this);
                    sub.setText(String.format("Sub Date: %d/%d/%d Cost: $%.2f",
                            subPaymentsInMonth.get(subsIndex).time.getDate(),
                            subPaymentsInMonth.get(subsIndex).time.getMonth(),
                            subPaymentsInMonth.get(subsIndex).time.getYear() + 1900,
                            subPaymentsInMonth.get(subsIndex).amount));
                    serviceListLayout.addView(sub);
                    */
                    subsIndex++;
                }
                serviceListLayout.addView(outerLayout);
            }

            TextView totalText = findViewById(R.id.paymentHistoryTotal);
            totalText.setText(String.format("Total: -$%.2f", total));
            totalText.setTextColor(getResources().getColor(R.color.negativePayText));
            totalText.setVisibility(View.VISIBLE);
        }
        else{
            TextView noHistoryText = new TextView(this);
            noHistoryText.setText("No Services In This Month");
            serviceListLayout.addView(noHistoryText);
            TextView totalText = findViewById(R.id.paymentHistoryTotal);
            totalText.setVisibility(View.INVISIBLE);
        }
    }
}