package com.hjungwoo01.calendarapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.hjungwoo01.calendarapp.model.File;
import com.hjungwoo01.calendarapp.model.Memo;
import com.hjungwoo01.calendarapp.model.User;
import com.hjungwoo01.calendarapp.retrofit.FileApi;
import com.hjungwoo01.calendarapp.retrofit.MemoApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewMemoActivity extends AppCompatActivity {
    private static final int REQUEST_FILE_PERMISSION = 1;
    private TextView receiversTextView;
    private boolean[] selectedReceivers;
    private List<Integer> receiversList = new ArrayList<>();
    private final String[] receiversArray = User.getUsersStringArray();
    private TextInputEditText inputEditMemoName;
    private TextInputEditText inputEditMemo;
    private MaterialButton buttonSave;
    private String receiversString;
    private ImageView selectedImageView;
    private Button buttonSelectFile;
    private Bitmap bitmap;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memo);
        initializeComponents();
    }

    private void initializeComponents() {
        receiversTextView = findViewById(R.id.form_receiversTextView);
        inputEditMemoName = findViewById(R.id.form_textFieldMemoName);
        inputEditMemo = findViewById(R.id.form_textFieldMemo);
        buttonSave = findViewById(R.id.form_buttonSave);
        selectedImageView = findViewById(R.id.selectedImageView);
        buttonSelectFile = findViewById(R.id.select_file);

        selectedReceivers = new boolean[receiversArray.length];

        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);
        FileApi fileApi = retrofitService.getRetrofit().create(FileApi.class);

        final AtomicReference<byte[]> fileDataRef = new AtomicReference<>(null);
        final String[] selectedFileType = new String[1];

        receiversTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewMemoActivity.this);

                builder.setTitle("Send Memo To: ");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(receiversArray, selectedReceivers, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked) {
                            receiversList.add(which);
                        } else {
                            receiversList.remove(Integer.valueOf(which));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        for(int i  = 0; i < receiversList.size(); i++) {
                            sb.append(receiversArray[receiversList.get(i)]);
                            if(i != receiversList.size() - 1) {
                                sb.append(",");
                            }
                        }
                        receiversString = sb.toString();
                        receiversTextView.setText(receiversString);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i < selectedReceivers.length; i++) {
                            selectedReceivers[i] = false;
                            receiversList.clear();
                            receiversTextView.setText("");
                        }
                    }
                });
                builder.show();
            }

        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if(data != null && data.getData() != null) {
                                Uri uri = data.getData();
                                selectedFileType[0] = getContentResolver().getType(uri);
                                fileDataRef.set(readFileData(uri));
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                    selectedImageView.setImageBitmap(bitmap);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                });

        buttonSelectFile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewMemoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startFileSelection();
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(NewMemoActivity.this);
                        builder.setTitle("Permission Required")
                                .setMessage("This app requires permission to access external storage in order to select files.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(NewMemoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                        Snackbar.make(v, "This app requires permission to access external storage in order to select files.", Snackbar.LENGTH_LONG)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(NewMemoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
                                    }
                                })
                                .show();
                    }
                    ActivityCompat.requestPermissions(NewMemoActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_FILE_PERMISSION);
                }
            }
        });

        buttonSave.setOnClickListener(view -> {
            String memoName = String.valueOf(inputEditMemoName.getText());
            String memo = String.valueOf(inputEditMemo.getText());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);
            String date = sdf.format(new Date());

            Memo newMemo = new Memo();
            newMemo.setOwner(OwnerSelectionActivity.getSelectedOwner());
            newMemo.setReceiver(receiversString);
            newMemo.setMemoName(memoName);
            newMemo.setMemo(memo);
            newMemo.setDate(date);

            memoApi.save(newMemo).enqueue(new Callback<Memo>() {
                @Override
                public void onResponse(@NonNull Call<Memo> call, @NonNull Response<Memo> response) {
                    if (response.isSuccessful()) {
                        Memo savedMemo = response.body();
                        if (savedMemo != null) {
                            File file = new File();
                            file.setMemoId(savedMemo.getId());
                            file.setName(savedMemo.getMemoName());
                            file.setType(selectedFileType[0]);
                            file.setData(fileDataRef.get());

                            fileApi.upload(file).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(NewMemoActivity.this, "File upload successful.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(NewMemoActivity.this, "File upload failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                    Toast.makeText(NewMemoActivity.this, "File upload failed.", Toast.LENGTH_SHORT).show();
                                    Log.e("NewMemoActivity", "Error occurred while saving file: " + t.getMessage());
                                }
                            });

                            Toast.makeText(NewMemoActivity.this, "Memo saved.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(NewMemoActivity.this, MemoActivity.class));
                            finish();
                        } else {
                            Toast.makeText(NewMemoActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(NewMemoActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Memo> call, @NonNull Throwable t) {
                    Toast.makeText(NewMemoActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                    Log.e("NewMemoActivity", "Error occurred while saving memo: " + t.getMessage());
                }
            });
        });
    }

    private void startFileSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FILE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startFileSelection();
            } else {
                Toast.makeText(this, "Permission denied. Unable to select file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] readFileData(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }
                inputStream.close();
                return byteArrayOutputStream.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void backToMemoMain(View view) {
        startActivity(new Intent(NewMemoActivity.this, MemoActivity.class));
    }
}
