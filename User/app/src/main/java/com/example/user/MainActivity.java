package com.example.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    private ImageView icons[] = new ImageView[4];
    private TextView texts[] = new TextView[4];

    private int pageNumber = 0;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        icons[0] = findViewById(R.id.imageView1);
        icons[1] = findViewById(R.id.imageView2);
        icons[2] = findViewById(R.id.imageView3);
        icons[3] = findViewById(R.id.imageView4);

        texts[0] = findViewById(R.id.textView1);
        texts[1] = findViewById(R.id.textView2);
        texts[2] = findViewById(R.id.textView3);
        texts[3] = findViewById(R.id.textView4);

        Log.d("Main", "Retriving documents from firebase");

        db = FirebaseFirestore.getInstance();
        CollectionReference pagesCollection = db.collection("pages");
        pagesCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    for (QueryDocumentSnapshot document: task.getResult()) {
                        if (i < pageNumber * 4) {
                            continue;
                        }

                        String pageId = document.getId().toString();
                        //Log.d("TAG", pageId + " => " + document.getData());
                        Log.d("Main", "Got document: " + document.getData().get("name").toString());

                        if (document.getData().get("name") != null) {
                            texts[i].setText(document.getData().get("name").toString());
                            texts[i].setVisibility(View.VISIBLE);
                        }

                        if (document.getData().get("icon") != null) {
                            String base64Img = document.getData().get("icon").toString();
                            Log.d("Main", "Decode icon: " + (base64Img.length() < 60 ? base64Img : base64Img.substring(0, 59)));
                            if (base64Img.length()>0 && ! "Not implemented".equalsIgnoreCase(base64Img)) {
                                if (base64Img.startsWith("data:image/jpeg;base64,")) {
                                    base64Img = base64Img.substring("data:image/jpeg;base64,".length());
                                }

                                try {
                                    base64Img = base64Img.replaceAll("\n", "");
                                    byte[] imgData = Base64.getDecoder().decode(base64Img);
                                    Bitmap image = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
                                    icons[i].setImageBitmap(image);
                                    icons[i].setVisibility(View.VISIBLE);

                                }
                                catch (Exception e) {
                                    Log.e("Main", "Error decoding image", e);
                                }

                                icons[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Goto page ...
                                        if (document.getData().get("type") != null) {
                                            String pageType = document.getData().get("type").toString();
                                            Log.d("Main", "Navigate to page with type " + pageType + " and id " + pageId);
                                            if ("textPage".equalsIgnoreCase(pageType)) {
                                                Intent intent = new Intent(MainActivity.this, TextPage.class);
                                                intent.putExtra("PageId", pageId);
                                                startActivity(intent);

                                            }
                                            else if ("dropdown".equalsIgnoreCase(pageType)) {
                                                Intent intent = new Intent(MainActivity.this, DropdownPage.class);
                                                intent.putExtra("PageId", pageId);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        i++;
                        if (i == 4) {
                            // more than more page
                            break;
                        }

                    }
                }
                else {
                    Log.w("Main", "Error getting firebase documents", task.getException());
                }
        }});
    }

    private String loadData() {
        // load data from firebase
        String imgString = "iVBORw0KGgoAAAANSUhEUgAAAVwAAAHQCAIAAABiBkXYAAAAAXNSR0IArs4c6QAAAANzQklUCAgI"+
                "2+FP4AAAEsRJREFUeJzt3U+PZOdVx/HfOc+t6mmPZ2zHURxPICKWgnEQLHCygWyRkLJmxRohhbwC"+
                "3gBLWPMWYAWsEJtIgSwQESTYcSwk4iAR/3diz0x3Vd3nHBbVk8yJlEzdnr7dNdXfz8KaGdWte91d"+
                "9b331vRzxvI0BQAP+FUfAID9QhQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAU"+
                "RAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABRE"+
                "AUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQB"+
                "QEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFA"+
                "QRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBB"+
                "FAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEU"+
                "ABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQA"+
                "FEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAU"+
                "RAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABRE"+
                "AUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQB"+
                "QDFc9QFcF5mZmev1Zvu7Kz4a4JcjCpdntVq98867V30UwCNw+3BJzMzMrvoogEfjSuEqJC3G/iIK"+
                "V8Hiqo8A+KU4ZQEoiAKAgigAKIgCgIIoACiIAoCCKAAoiAKAgigAKIgCgIIoAChY+3BJ0qRsykE2"+
                "siDqQFhPLWQbyWzmERmeHrlQu59qk/c18fVGFM7FQumT1jWZlN5lG0muPtuR4RKlSaFsltHn7ryt"+
                "5CfKI5k8p1Zh2uuNKEyxbYEkjavFdzI3slTuNCXBUqdHJ6tn3pM0+wsIl8NOPJZvfi/vfXh77s6b"+
                "Wdz6lrLJVuPOgznu3Llz586dqfsiCudisfJvp9bSKA3Soy8ZMu3u8Mn95z5Mi7kvNXE50tzCv/uj"+
                "j//vzc9Ly3l35iu98NdhfXvRueNGrz7ze59+9is58cqCKEzx0L1ZSKGNNOx6bWa5iEGiCIdklA2y"+
                "blrOP3dz0/NIdiJJmneEFxeyAAqiAKAgCgAKogCgIAoACqIAoCAKAAqiAKAgCgAKogCgIAoACqIA"+
                "oCAKAAqiAKAgCgCKQ5uncPfu3bt3782+Gz/56QvvpE5238KkUPf07oxUwF47tCiM43h6ejr7bvz0"+
                "XtxXu7/jLDZJloMkMXYJe+/QonBpWrp62/3+q3tIogjYf0ThnEKaNM25ZUgKueS7zHQErgpReBwT"+
                "PqaNnz+YImCv8bcPAAqiAKAgCgAKogCgIAoACqIAoCAKAAqiAKAgCgAKogCgIAoAisNa+5BSNqXL"+
                "wmdYj9hdLVK56K5FqPsmdOQag7ZeS5aDbGPpspVyOeu+UoPbiaLJ+tzL7w8uCr7qi58ojvscT28R"+
                "HtJa1kNN8bTLpQVJuJ7MTrzf8OgtpZx3nVvXqH5bfk/ZPHd923ocexxnTns3WJ4ezhL/9PGd8Vvv"+
                "5t+nr2zn8ScTnl8uuWV4Lj76u0/95MO7LHm8znredP/k/fc+2axNMcdp6GGu9rHihpSyccdtbt26"+
                "devWral7OqgrBUvJVr19nLLZPi5x00bj0x989PH7P75vWqfcSMM19aHkymFQ+Bz3qw8JS+Xz8lNJ"+
                "yhs7bnXySZ588vGkwR86sCgoB7NVqtls13Ke0d2kcKnlyMDF680lbc/bl/G5kq0f7HHekxC3wwAK"+
                "ogCgIAoACqIAoCAKAAqiAKAgCgAKogCgIAoACqIAoCAKAAqiAKAgCgAKogCgIAoACqIAoCAKAAqi"+
                "AKAgCgAKogCgIAoACqIAoCAKAAqiAKAgCgAKogCgIAoACqIAoCAKAAqiAKAgCgCK4TJ2YqNykIVy"+
                "YoNy4o4y02409ZBN3BLAmfmjYJF9MJdsHWqTNr2/vjttXx4r28iP5KvJQQEg6RKiEOHWYrX4l5X9"+
                "W6pP2vat8X8nPd7Tw1bRThTHUkzaFsDWJVwppNKVLex06tt0HFYTtwhLVxxRBODcZo+Cy2SROe0a"+
                "Ycsm3wLwuSnwuHgXASiIAoCCKAAo5o/Cg88F+OgPeCJwpQCgIAoACqIAoCAKAAqiAKAgCgAKogCg"+
                "IAoACqIAoCAKAIrh9PR00gaZeXR05E5NgMM0vP32O1O3+exnP3vjxtEcRwPgynHCB1AQBQAFUQBQ"+
                "EAUABVEAUBAFAAVRAFAQBQAFUQBQEAUABVEAUBAFAAVRAFAQBQAFUQBQDFd9AE+YMLeMtGjZQlJY"+
                "Kj0fuZ3M7OwXqW60GPuLKJxHd7371c/cv/vpsyrYTludnJy88/47aWk7RAS4KkRhgjS1UJjS9MFX"+
                "Xux5I6y7tONd2Ed3f/LGGx90T5qAfcZ17Hl0k0thSvmuX8O07YbdY96DAx4PVwoTWCqsNGDIs0A8"+
                "elvJUum9BSHGXuMFCqAgCgAKogCgIAoACqIAoCAKAAqiAKAgCgAKogCgIAoACqIAoJh97UMeaHYs"+
                "3eVSd2mXtdNpmRbKJrFIEntt9ihsxw2ESYd1WdItQt0VIfkO8xQ8Fi3cUtpt+AJwVea/Ukhtz42p"+
                "iAPKQkvvap4PzVT6ldIzLNJoAvbd8Prrr0/aIDOfeuqpYXhux8dbeFh0H9MztJm0r6nHdincNa78"+
                "6L9uPre245axnbnySJba9LWUnmfXTcB+Gl577bWp27z88su3bt3a/fFpOUZf63TqBwznOLb5nUXh"+
                "+596fuVHpq5s2mG+WmYul8tnnnmGImDPHc71PIALQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQB"+
                "QEEUABREAUBxiVE41MkKwGHhjQqgIAoACqIAoBjONTIwp2xlLba/cCmm72u/pI2Rw9n8BOuKnYYp"+
                "AE+QYdnemrrNsr21bOPOD3cpNAyjvTx1R+Nm8iaz8ozuauFjc8mVThFweIZh+YNJG2TmsPxBW97d"+
                "dYPt5FY91/VblotJ++qr3570+Ll1KWVK9Sbl/bM/TSMNOCSzD24NNc/wWKi1tN2vL/ZXmKQuGcPa"+
                "cZCGqT8+YJLSd9/KJZnSIzPNnuzPNT3VPT0keVie/YntMrcVeGI82e9SABeOKAAoiAKAgigAKIgC"+
                "gIIoACiIAoCCKAAoiAKAgigAKIgCgIIo7ClP5fa/tndDKLqdHd41XxCWUgt5qsXeLX/xlKSUjT75"+
                "2GZfJYnz6aaWkmT7N/B22dVde/c+uHQmrZtkkdIQe/dtSsmURz36xIWIk6OQec1PD5dkW4R1s+PR"+
                "xj17vW0GSToadTLkENf39dBCvWk5utxizyIZLk+Za8yYOu5j+PNvfGOeo8Jj6R6WfmPj//gXf6V+"+
                "1UdTbS9Nw/QHf/bHT3/xxUnb3r17980335zlsB5bZn75y1/e/bQ3tlhE+/bf/O1H//3jfbuX8lSY"+
                "Xv7Dr/zGH/3+1GPj9mFPbe8aRg+LvfsXM0KSycI3bfLnHft8pWlmkw7PUiGlmVnu21CNTMnUzZV9"+
                "6keHe/ZyA3DViAKAgigAKIgCgIIoACiIAoCCKAAoiAKAgigAKIgCgIIoACiIAoCCKAAoiAKAgigA"+
                "KGafpzAq3dzz1O34bL6Y7br0fOibSfsyzx6LcXCfvoZ833RTC3Olxqapo3Omymzp/agPo8Y2765a"+
                "6GizSNsOjLj48ZOe6i5Jva2mjiGbKuVmm2Hjl/A9slQuQtHks4/cmT0KQ6bCb+Z4HO+7pr3J//JP"+
                "/2TS49fKb37njX/+j/9U+v6N0pzGFTK/sV489f1+tJn3f6bbYrNY33+ljYs+9wChL7316qv/+ruj"+
                "LZaRfefTw+6GyFWzITff/fwPv/eFf7/w53+YKTwHf6/ffL0vx3m/R6fL3HxRmxuXMYRr/slLpjAz"+
                "Wy/6KJu6u9WkR9+wUK66STb7yXVuQ6hbrN2HsQ193tP3YL1JJ+mKnHt4tNvJ7ZNbp8PCMhZ2MS/x"+
                "MvUo283Rj/rgdnIhT/4rdFdPtXG4fXJj9Hm/bsNGJ22UooX3ufc167NLUl+4reXWbdHmPg2lpwbJ"+
                "h5g8wXbfjNaG3LjGlqG5h4Jma9HTQzn7/NGhDyeLNkS3tK6Lid3DQ+HC5OqnQxv68kKe/Ffu1z3D"+
                "rG/aOPdJyNUlV1if/xJ4/ih4l5pSsxdhuzfrSssn/AMFSaYcXcuNx/x1M/W1DYpRwyz3+Q/b3vBb"+
                "qpsu6nKu3CqmhclSc59OJaUU5ml+CZelaTJlWl7CRfAT/+YBcLGIAoCCKAAoiAK2tvf6e/fvVuLy"+
                "EQUABVEAUBAFAAVRAFAQBQAFUQBQEAUAxfxrHy7Tg5+BD4s5erf9Gfuzn7R/eGneDj+MnpKnpWTa"+
                "7djSdLbqIXRBC4d+OZNC29Wlu/2kQkotwp/wpaiPr4UuZ5F+C1dKKVfs/sMkca4fPDmsKEg6K8Ms"+
                "V0Ce6qbtOyG9h0upePAnjxAW1sNj0fdu1kNatjSlhnEY2/joDUxKH1uMft2vNPOyvpWjW7SU1H2n"+
                "RWuWaiFP7+ZtYrsPKwqRt28+fefFX3PFTqfvc+zB5KmN5eef3aw9W6j7bsvW0tPXp7HDW+4hZvbS"+
                "Sy8t5x6HJI1N/Yv30lvLRx/hz+Ym3Vws5j6wPRemz9x58eZLx7PvyfLNL6ybr0YbbIcodJOnHz/3"+
                "zLKfrUzd3WFFweyVV1554Xe+OtP6X5O65Jkb0+d0stbZRLFdvuah4e17P/rmG98KmzAqLiK+/vWv"+
                "LzbznpBb6pOj/KdX3rq/3Kmkntq4DdJofeaV1vsupa997WuvvHp77h2tjsZ/+NLbY9vEbhOEPCVZ"+
                "N2n6aMIDi0KY2XwTAXL71TWTwpSSW8p3u4B0xZDbSX67fYfsbLV+5uw37t1z6G1sqd1W64epZabU"+
                "8rrfPphi9O15Yd46WjcpQpa20y1LmLY3N+eYNnTdv6kAfgFRAFAQBQAFUQBQEAUABVEAUBAFAAVR"+
                "AFAQBQAFUQBQEAUABVEAUBAFAAVRAFAQBQDFYc1TkC9y9Ux8lLEw6xf+7Nt17J5+6uneNOc0rhY2"+
                "NoX02osft5nnt1lqNaRFk1/vkSlTWSja27dXQ3y4mXk41thyCN072hyvF+PU+WoTHVYUejvOzbFO"+
                "FKl28W+ksEyzFrphLj1/4c//sPuLXPbF6SK+/8JPNUPgHubpHu10uWp92LPxkXuthbpv3r3dP3j6"+
                "JGb+HkmuNI/FyTIXM+/qsKLQNpkWataaT5h5uyvfDokODTb7gOVnT3V/ISnTR+W8oxDDergNffBo"+
                "4XO/uA9HaNGih8W6ZYt5XxBmq27LFvb0WiczT8Y8qChkpsmbUrHZdUzaNCGF3MedRrU+lvsLhW+O"+
                "N75qR6Z536hpCtMix+7iSmF3aRGSpx+N1ucedx9HS/WU7i4nT2ee6qCiYD87fZvNM8zZH+xo8eC3"+
                "40wf1prUYlg3zV0ESZbelNsLIOxu+yIL027zvB9zZ9nlkuYugvjbBwC/gCgAKIgCgIIoACiIAoCC"+
                "KAAoiAKAgigAKIgCgIIoACiIAoCCKAAoiAKAgigAKIgCgIIoACiIAoCCKAAoiAKAgigAKIgCgIIo"+
                "ACiIAoCCKAAoiAKAgigAKIgCgIIoACiIAoCCKAAoiAKAYrjqA3iimJSSNIReVAsptPDtH+1gOLVP"+
                "/c9p99Hy+rbYNz8+/s1PZ26G9O5x4c/fUmmhHH79eVssPjdpWzN7+of3L/yQnjhE4ZxeytatSZJS"+
                "skc+PtMW6+HF93paWva5D29vhX1w8pmPV8sTpQ9x8XEc2yjF0frm7WHz7MlTk7Y1s7x/fb81P0MU"+
                "zinVJE+5aafTnVm3zBYmte67XlwcniGst83QF60P47C58Oc/Wh/3tultM4TCpkUnU7v0/eARhXMy"+
                "dUmm2PFKwdPMvHumqV38VfMTIyzTokUbh5M5bqPGxb3Wj7r3kPwa36Y9DqIwxYMTvKe6ecpj549q"+
                "uymUkllmXO+zkaWHpeUsrz2LZVhuc3PNv87nRkoBFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEU"+
                "ABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQAFEQBQEEUABREAUBBFAAURAFAQRQA"+
                "FEQBQEEUABREAUBBFAAURAFAQRQAFP8P8aadWyq8ocQAAAAASUVORK5CYII=";

        return imgString;
    }
}