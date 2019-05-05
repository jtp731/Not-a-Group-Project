package com.example.roadsideassistance;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CustomerServiceFinish extends AppCompatActivity {
    Customer customer;
    Service selectedService;
    AppDatabase database;
    Car serviceCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service_finished_select);
        database = AppDatabase.getDatabase(this);

        customer = getIntent().getParcelableExtra("Customer");
        selectedService = getIntent().getParcelableExtra("Service");

        TextView displayLine = findViewById(R.id.customerSelectedFinishedRoadsideUsername);
        displayLine.setText(selectedService.roadside_assistant_username);

        serviceCar = customer.getCar(selectedService.car_plateNum);
        displayLine = findViewById(R.id.customerSelectedFinishedCarManufacturer);
        displayLine.setText(serviceCar.manufacturer);

        displayLine = findViewById(R.id.customerSelectedFinishedCarPlate);
        displayLine.setText(selectedService.car_plateNum);

        displayLine = findViewById(R.id.customerSelectedFinishedModel);
        displayLine.setText(serviceCar.model);

        displayLine = findViewById(R.id.customerSelectedFinishedCost);
        if(customer.carCoveredBySubscription(selectedService.car_plateNum)) {
            displayLine.setText("Covered By Subscription");
            Button payButton = findViewById(R.id.customerFinishServiceButton);
            payButton.setText("Finish");
        }
    }

    public void finishService(View view) {
        if(customer.bankAccount.pay(database.roadsideAssistantDao().getRoadsideAssistant(selectedService.roadside_assistant_username), customer, selectedService.car_plateNum, selectedService.cost)) {
            customer.finishService(selectedService);
            if(customer.carCoveredBySubscription(selectedService.car_plateNum))
                database.serviceDao().updateServiceStatus(selectedService.roadside_assistant_username, customer.username, selectedService.car_plateNum, selectedService.time, 4);
            else
                database.serviceDao().updateServiceStatus(selectedService.roadside_assistant_username, customer.username, selectedService.car_plateNum, selectedService.time, 3);
            finish();
        }
        else
            Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show();
    }
}