package com.checkmybill.presentation.IntroFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.checkmybill.R;
import com.checkmybill.util.IntentMap;
import com.checkmybill.util.NotifyWindow;
import com.checkmybill.util.SharedPrefsUtil;

/**
 * Created by Victor Guerra on 09/03/2016.
 */
public class IntroFragment extends Fragment {

    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String PAGE = "page";

    private int mBackgroundColor, mPage;
    private ImageView intro_img;
    private Button btnJumpToLogin, btnJumpToCreateAccount, btnRunWithoutLogin;
    private boolean ignoreJumpButton;

    public static IntroFragment newInstance(int backgroundColor, int page) {
        IntroFragment frag = new IntroFragment();
        Bundle b = new Bundle();
        b.putInt(BACKGROUND_COLOR, backgroundColor);
        b.putInt(PAGE, page);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ignoreJumpButton = getActivity().getIntent().getBooleanExtra("HIDDEN_JUMP_BUTTON", false);

        if (!getArguments().containsKey(BACKGROUND_COLOR)) throw new RuntimeException("Fragment must contain a \"" + BACKGROUND_COLOR + "\" argument!");
        mBackgroundColor = getArguments().getInt(BACKGROUND_COLOR);

        if (!getArguments().containsKey(PAGE)) throw new RuntimeException("Fragment must contain a \"" + PAGE + "\" argument!");

        if ( ignoreJumpButton == true ) mPage = 3;
        else mPage = getArguments().getInt(PAGE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Select a layout based on the current page
        int layoutResId;
        switch (mPage) {
            case 0:
                layoutResId = R.layout.intro_fragment_layout_1;
                break;
            case 1:
                layoutResId = R.layout.intro_fragment_layout_2;
                break;
            case 2:
                layoutResId = R.layout.intro_fragment_layout_3;
                break;
            default:
                layoutResId = R.layout.intro_fragment_layout_4;
        }

        // Inflate the layout resource file
        View view = getActivity().getLayoutInflater().inflate(layoutResId, container, false);

        // Set the current page index as the View's tag (useful in the PageTransformer)
        view.setTag(mPage);

        // Checando se esta na pagina final (login or create)
        if ( mPage == 3 ) {
            btnJumpToLogin = (Button) view.findViewById(R.id.btnJumpToLogin);
            btnJumpToCreateAccount = (Button) view.findViewById(R.id.btnJumpToCreateAccount);
            btnRunWithoutLogin = (Button) view.findViewById(R.id.btnRunWithoutLogin);

            btnJumpToLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent it = new Intent(IntentMap.LOGIN);
                    getActivity().startActivity(it);
                }
            });
            btnJumpToCreateAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent it = new Intent(IntentMap.CREATE_ACCOUNT);
                    getActivity().startActivity(it);
                }
            });
            btnRunWithoutLogin.setOnClickListener(this.btnRunWithoutLoginClickEvent);

            // Checando se deve ocultar o botao...
            if ( ignoreJumpButton ) btnRunWithoutLogin.setVisibility(View.GONE);

            return view;
        }

        // Execucao normal, alimentando a imagem de exemplo
        intro_img = (ImageView) view.findViewById(R.id.intro_img);
        int imgResId;
        switch (mPage) {
            case 0:
                imgResId = R.drawable.qualidade_de_sinal;
                break;
            case 1:
                imgResId = R.drawable.home;
                break;
            default:
                imgResId = R.drawable.analytics_list;
        }
        intro_img.setImageBitmap(decodeSampledBitmapFromResource(getResources(), imgResId, 100, 100));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the background color of the root view to the color specified in newInstance()
        View background = view.findViewById(R.id.intro_background);
        background.setBackgroundColor(mBackgroundColor);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private View.OnClickListener btnRunWithoutLoginClickEvent = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Confirmando ação do usuário...
            AlertDialog.Builder dlgBuilder = new NotifyWindow(getContext()).getBuilder();
            dlgBuilder.setTitle("Entrar");
            dlgBuilder.setMessage("Ao entrar sem realizar o login, alguns recursos não estarão disponíveis.\n\nTem certeza que deseja continuar?");
            dlgBuilder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    new SharedPrefsUtil(getContext()).setJumpLogin(true);
                    Intent it = new Intent(IntentMap.HOME);
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(it);
                }
            });
            dlgBuilder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dlgBuilder.create().show();
        }
    };
}
