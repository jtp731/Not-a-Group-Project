package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class RoadsideSelectedActiveService extends AppCompatActivity {
    RoadsideAssistant roadsideAssistant;
    Service activeService;
    Car car;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadside_selected_active);
        database = AppDatabase.getDatabase(this);

        roadsideAssistant = getIntent().getParcelableExtra("Roadside");
        activeService = getIntent().getParcelableExtra("Service");

        car = database.carDao().getCar(activeService.customer_username, activeService.car_plateNum);

        TextView customerTextView = findViewById(R.id.roadsideSelectedActiveServiceCutomer);
        TextView carTextView = findViewById(R.id.roadsideSelectedActiveServiceCar);
        TextView costTextView = findViewById(R.id.roadsideSelectedActiveServiceCost);
        TextView descriptionView = findViewById(R.id.description);

        customerTextView.setText("Username: " + activeService.customer_username);
        carTextView.setText("Plate Number: " + activeService.car_plateNum);
        costTextView.setText("Pay: "+ activeService.costToPay());

        descriptionView.setText(activeService.descriptionString());
    }

    public void cancelService(View view) {
        roadsideAssistant.removeService(activeService);
        database.serviceDao().setServiceToOpen(activeService.customer_username, activeService.car_plateNum, activeService.time);
        database.serviceDao().deleteService(activeService.roadside_assistant_username, activeService.customer_username, activeService.car_plateNum, activeService.time);
        finish();
    }

    public void finishService(View view) {
        roadsideAssistant.updateService(activeService, 2);
        database.serviceDao().updateService(roadsideAssistant.username, activeService.customer_username, activeService.car_plateNum, activeService.time, 2);
        database.serviceDao().deleteServicesNotEqual(roadsideAssistant.username, activeService.customer_username, activeService.car_plateNum, activeService.time);
        finish();
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra("Roadside", roadsideAssistant);
        setResult(RESULT_OK, data);
        super.finish();
    }
}
