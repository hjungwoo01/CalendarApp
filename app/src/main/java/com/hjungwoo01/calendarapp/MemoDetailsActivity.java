package com.hjungwoo01.calendarapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.bumptech.glide.Glide;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class MemoDetailsActivity extends AppCompatActivity {
    private static final int REQUEST_FILE_PERMISSION = 1;
    private Memo memo;
    private TextView senderLabel;
    private TextView senderTextView;
    private TextView receiversTextView;
    private TextInputEditText inputEditMemoName;
    private TextInputEditText inputEditMemo;
    private boolean[] selectedReceivers;
    private List<Integer> receiversList = new ArrayList<>();
    private final String[] receiversArray = User.getUsersStringArray();
    private String receiversString;
    private ImageView selectedImageView;
    private Button buttonSelectFile;
    private MaterialButton updateButton;
    private MaterialButton deleteButton;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private final AtomicReference<byte[]> fileDataRef = new AtomicReference<>(null);
    private final String[] selectedFileType = new String[1];
    private RetrofitService retrofitService;
    private MemoApi memoApi;
    private FileApi fileApi;
    private boolean hasMemoFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_details);
        initWidgets();
        selectedImageView.setVisibility(View.GONE);

        boolean hideButtons = getIntent().getBooleanExtra("hideButtons", false);
        if(hideButtons) {
            buttonSelectFile.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);

            receiversTextView.setEnabled(false);
            inputEditMemoName.setEnabled(false);
            inputEditMemo.setEnabled(false);

            int black = ContextCompat.getColor(this, R.color.black);
            receiversTextView.setTextColor(black);
            inputEditMemoName.setTextColor(black);
            inputEditMemo.setTextColor(black);
        } else {
            senderLabel.setVisibility(View.GONE);
            senderTextView.setVisibility(View.GONE);
        }

        long memoId = getIntent().getLongExtra("memoId", -1);
        String owner = getIntent().getStringExtra("memoSender");
        senderTextView.setText(owner);

        if (memoId != -1) {
            retrofitService = new RetrofitService();
            memoApi = retrofitService.getRetrofit().create(MemoApi.class);
            fileApi = retrofitService.getRetrofit().create(FileApi.class);
            fetchMemoDetails(memoId);
            fetchMemoFile(memoId);
            initializeComponents(memoId);
        } else {
            Toast.makeText(this, "Memo ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initWidgets() {
        senderLabel = findViewById(R.id.form_senderLabel);
        senderTextView = findViewById(R.id.form_senderTextView);
        receiversTextView = findViewById(R.id.form_receiversTextView);
        inputEditMemoName = findViewById(R.id.form_textFieldMemoName);
        inputEditMemo = findViewById(R.id.form_textFieldMemo);
        selectedImageView = findViewById(R.id.selectedImageView);
        buttonSelectFile = findViewById(R.id.select_file);
        deleteButton = findViewById(R.id.form_buttonDelete);
        updateButton = findViewById(R.id.form_buttonUpdate);
        selectedReceivers = new boolean[receiversArray.length];
    }

    private void initializeComponents(long memoId) {
        receiversTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MemoDetailsActivity.this);

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
                            assert data != null;
                            Uri uri = data.getData();
                            selectedImageView.setVisibility(View.VISIBLE);
                            selectedFileType[0] = getContentResolver().getType(uri);
                            fileDataRef.set(readFileData(uri));
                            Glide.with(MemoDetailsActivity.this)
                                    .load(fileDataRef.get())
                                    .into(selectedImageView);
                        }
                    }
                });

        buttonSelectFile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MemoDetailsActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startFileSelection();
                } else {
                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MemoDetailsActivity.this);
                        builder.setTitle("Permission Required")
                                .setMessage("This app requires permission to access external storage in order to select files.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(MemoDetailsActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                        Snackbar.make(v, "This app requires permission to access external storage in order to select files.", Snackbar.LENGTH_LONG)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(MemoDetailsActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_FILE_PERMISSION);
                                    }
                                })
                                .show();
                    }
                    ActivityCompat.requestPermissions(MemoDetailsActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_FILE_PERMISSION);
                }
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String memoName = String.valueOf(inputEditMemoName.getText());
                String memo = String.valueOf(inputEditMemo.getText());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);
                String date = sdf.format(new Date());

                Memo updatedMemo = new Memo();
                updatedMemo.setOwner(OwnerSelectionActivity.getSelectedOwner());
                updatedMemo.setReceiver(receiversString);
                updatedMemo.setMemoName(memoName);
                updatedMemo.setMemo(memo);
                updatedMemo.setDate(date);

                updateMemo(memoId, updatedMemo);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(memoId);
            }
        });
    }

    private void fetchMemoDetails(long memoId) {
        memoApi.getMemo(memoId).enqueue(new Callback<Memo>() {
            @Override
            public void onResponse(@NonNull Call<Memo> call, @NonNull Response<Memo> response) {
                if (response.isSuccessful()) {
                    memo = response.body();
                    if (memo != null) {
                        displayMemoDetails();
                    } else {
                        Toast.makeText(MemoDetailsActivity.this, "Memo not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(MemoDetailsActivity.this, "Failed to fetch memo details.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Memo> call, @NonNull Throwable t) {
                Toast.makeText(MemoDetailsActivity.this, "Failed to fetch memo details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMemoFile(long memoId) {
        fileApi.getFileByMemoId(memoId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            InputStream inputStream = responseBody.byteStream();
                            showMemoFileImage(inputStream);
                            hasMemoFile = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MemoDetailsActivity.this, "Error occurred while reading the response body.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MemoDetailsActivity.this, "Failed to fetch memo file.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(MemoDetailsActivity.this, "Failed to fetch memo file from server.", Toast.LENGTH_SHORT).show();
                if (t instanceof HttpException) {
                    int responseCode = ((HttpException) t).code();
                    Log.e("MemoDetailsActivity", "HTTP error occurred: " + responseCode);
                } else {
                    Log.e("MemoDetailsActivity", "Error occurred while fetching memo file: " + t.getMessage());
                }
            }
        });
    }

    private void showMemoFileImage(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IOException("Invalid input stream");
        }
        selectedImageView.setVisibility(View.VISIBLE);
        byte[] byteArray = getByteArrayFromInputStream(inputStream);
        Glide.with(this)
                .load(byteArray)
                .into(selectedImageView);
    }

    private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void displayMemoDetails() {
        if(memo.getReceiver() != null) {
            receiversString = memo.getReceiver();
            receiversTextView.setText(receiversString);
        }
        if (memo.getMemoName() != null) {
            inputEditMemoName.setText(memo.getMemoName());
        }
        if (memo.getMemo() != null) {
            inputEditMemo.setText(memo.getMemo());
        }
    }

    private void startFileSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    private void updateMemo(long memoId, Memo memo) {
        memoApi.updateMemo(memoId, memo).enqueue(new Callback<Memo>() {
            @Override
            public void onResponse(@NonNull Call<Memo> call, @NonNull Response<Memo> response) {
                if (response.isSuccessful()) {
                    Memo updatedMemo = response.body();
                    File updatedFile = new File();
                    updatedFile.setMemoId(memoId);
                    assert updatedMemo != null;
                    updatedFile.setName(updatedMemo.getMemoName());
                    updatedFile.setType(selectedFileType[0]);
                    updatedFile.setData(fileDataRef.get());

                    if(hasMemoFile) {
                        fileApi.updateFileByMemoId(memoId, updatedFile).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(MemoDetailsActivity.this, "Memo and file updated.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MemoDetailsActivity.this, "Failed to update file.", Toast.LENGTH_SHORT).show();
                                }
                                startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                Toast.makeText(MemoDetailsActivity.this, "Failed to update file.", Toast.LENGTH_SHORT).show();
                                Log.e("MemoDetailsActivity", "Error occurred while updating file: " + t.getMessage());
                                finish();
                            }
                        });

                    } else {
                        fileApi.upload(updatedFile).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(MemoDetailsActivity.this, "File upload successful.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MemoDetailsActivity.this, "File upload failed.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                Toast.makeText(MemoDetailsActivity.this, "File upload failed.", Toast.LENGTH_SHORT).show();
                                Log.e("MemoDetailsActivity", "Error occurred while saving file: " + t.getMessage());
                            }
                        });
                    }
                    Toast.makeText(MemoDetailsActivity.this, "Update successful.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
                    finish();
                } else {
                    Toast.makeText(MemoDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Memo> call, @NonNull Throwable t) {
                Toast.makeText(MemoDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                Log.e("MemoDetailsActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    private void deleteMemo(long memoId) {
        memoApi.deleteMemo(memoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.e("delete memo", String.valueOf(memoId));
                    fileApi.deleteFileByMemoId(memoId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MemoDetailsActivity.this, "Memo and file deleted.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MemoDetailsActivity.this, "Failed to delete file.", Toast.LENGTH_SHORT).show();
                            }
                            startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            Toast.makeText(MemoDetailsActivity.this, "Failed to delete file.", Toast.LENGTH_SHORT).show();
                            Log.e("MemoDetailsActivity", "Error occurred while deleting file: " + t.getMessage());
                            startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(MemoDetailsActivity.this, "No file associated with the memo.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(MemoDetailsActivity.this, "Failed to delete memo.", Toast.LENGTH_SHORT).show();
                Log.e("MemoDetailsActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog(long memoId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Memo");
        builder.setMessage("Are you sure you want to delete this memo?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteMemo(memoId);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
        startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
    }
}
