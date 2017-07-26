package custom.camera.test.nick.barcodestudy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

public class BarCodeActivity extends AppCompatActivity
{

    private static final int PICK_IMAGE = 1;
    static final int CAMERA_REQUEST = 2;
    private static final String FILE_NAME = "temp.jpg";

    private ImageView mImageView;
    private BarcodeDetector mBarcodeDetector;
    private TextView mTextView;
    private Bitmap mBitmap;
    private Observable<Void> mObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code);


        mImageView = (ImageView) findViewById(R.id.imgview);
        setImageView();

        mTextView = (TextView) findViewById(R.id.txtContent);

        if (setBarcodeDetector()) return;

        mObservable = getAsinkTaskWithObservable();

        Button buttonProcess = (Button) findViewById(R.id.button);
        buttonProcess.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                detectAndPrinValue();
            }
        });

        FloatingActionButton fabGallery = (FloatingActionButton) findViewById(R.id.fabGallery);
        fabGallery.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pickImageFromGallery();
            }
        });

        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fabCamera);
        fabCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startCamera();
            }
        });
    }

    private Observable<Void> getAsinkTaskWithObservable()
    {
        return Observable.create(new ObservableOnSubscribe<Void>()
        {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Void> e) throws Exception
            {
                Frame frame = new Frame.Builder().setBitmap(mBitmap).build();
                SparseArray<Barcode> barcodes = mBarcodeDetector.detect(frame);
                try
                {
                    mTextView.setText(barcodes.valueAt(0).rawValue);
                } catch (Exception ex)
                {
                    mTextView.setText("No results");
                }
            }
        });
    }

    private void detectAndPrinValue()
    {
        mObservable.subscribe();
    }

    private boolean setBarcodeDetector()
    {
        mBarcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
//                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if (!mBarcodeDetector.isOperational())
        {
            mTextView.setText("Could not set up the detector!");
            return true;
        }
        return false;
    }

    private void setImageView()
    {
        mBitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.qrcode);
        mImageView.setImageBitmap(mBitmap);
    }


// Pick Image

    private void pickImageFromGallery()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == PICK_IMAGE)
            {
                if (data != null)
                {
                    Uri uri = data.getData();
                    try
                    {
                        mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        mImageView.setImageBitmap(mBitmap);
                    } catch (IOException e)
                    {
                        mTextView.setText("Could not read this image!");
                    }
                }
            }
            if (requestCode == CAMERA_REQUEST)
            {
                try
                {
                    mBitmap = BitmapFactory.decodeFile(getCameraFileUri().getPath());
                    mImageView.setImageBitmap(mBitmap);
                } catch (Exception e)
                {
                    mTextView.setText("Could not read this image!");
                }
            }
        }
    }

    private void startCamera()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getCameraFileUri());
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private Uri getCameraFileUri()
    {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return Uri.fromFile(new File(directory, FILE_NAME));
    }
}
