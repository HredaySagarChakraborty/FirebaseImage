package comp.example.hreday.firebaseimage;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button choose, save, display;
    private EditText editText;
    private ImageView imageView;
    private Uri imageUri;
    private static final int IMAGE_REQUEST = 1;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    StorageTask uploadTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        choose = findViewById(R.id.chooseButton);

        editText=findViewById(R.id.editTextId);
        save = findViewById(R.id.saveButton);
        display = findViewById(R.id.displayButton);


        databaseReference = FirebaseDatabase.getInstance().getReference("Upload:");
        storageReference = FirebaseStorage.getInstance().getReference("Upload:");

        imageView = findViewById(R.id.imageViewId);


        choose.setOnClickListener(this);
        save.setOnClickListener(this);
        display.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {


        if (view.getId() == R.id.chooseButton) {
            openFileChooser();


        } else if (view.getId() == R.id.saveButton) {

            if(uploadTask!=null && uploadTask.isInProgress()){
                Toast.makeText(this, "Uploading in progress", Toast.LENGTH_SHORT).show();
            }

            else{
                saveData();

            }




        } else if (view.getId() == R.id.displayButton) {

            Intent intent =new Intent(MainActivity.this,ImageActivity.class);
            startActivity(intent);


        }


    }



    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);


        }

    }

    public String getFileExtension(Uri imageUri){

        ContentResolver contentResolver=getContentResolver();

        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));


    }


    private void saveData() {


        final String editText1=editText.getText().toString().trim();

        if(editText1.isEmpty()){


            editText.setError("Enter the image name");
            editText.requestFocus();
            return;
        }

        StorageReference ref=storageReference.child(System.currentTimeMillis()+ " ."+ getFileExtension(imageUri));

    ref.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {


                Toast.makeText(MainActivity.this, "Unsuccessfull", Toast.LENGTH_SHORT).show();
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.

                Toast.makeText(MainActivity.this, "Succesfull", Toast.LENGTH_SHORT).show();


                Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());


                    Uri downloadUri=uriTask.getResult();




                Upload upload=new Upload(editText1,downloadUri.toString());
                String uploadId=databaseReference.push().getKey();

                databaseReference.child(uploadId).setValue(upload);
            }
        });



    }
}
