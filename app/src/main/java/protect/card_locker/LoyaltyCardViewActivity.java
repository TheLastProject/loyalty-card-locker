package protect.card_locker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;

import java.util.List;

import androidx.palette.graphics.Palette;
import protect.card_locker.preferences.Settings;

public class LoyaltyCardViewActivity extends AppCompatActivity
{
    private static final String TAG = "Catima";

    TextView cardIdFieldView;
    View bottomSheet;
    ImageView bottomSheetButton;
    TextView noteView;
    TextView groupsView;
    TextView storeName;
    ImageView barcodeImage;
    View collapsingToolbarLayout;
    int loyaltyCardId;
    LoyaltyCard loyaltyCard;
    boolean rotationEnabled;
    DBHelper db;
    ImportURIHelper importURIHelper;
    Settings settings;

    String cardIdString;
    BarcodeFormat format;

    boolean starred;
    boolean backgroundNeedsDarkIcons;
    boolean barcodeIsFullscreen = false;
    ViewGroup.LayoutParams barcodeImageState;

    private void extractIntentFields(Intent intent)
    {
        final Bundle b = intent.getExtras();
        loyaltyCardId = b != null ? b.getInt("id") : 0;
        Log.d(TAG, "View activity: id=" + loyaltyCardId);
    }

    private Drawable getIcon(int icon, boolean dark)
    {
        Drawable unwrappedIcon = AppCompatResources.getDrawable(this, icon);
        Drawable wrappedIcon = DrawableCompat.wrap(unwrappedIcon);
        if(dark)
        {
            DrawableCompat.setTint(wrappedIcon, Color.BLACK);
        }
        else
        {
            DrawableCompat.setTintList(wrappedIcon, null);
        }

        return wrappedIcon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        settings = new Settings(this);

        extractIntentFields(getIntent());

        setContentView(R.layout.loyalty_card_view_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db = new DBHelper(this);
        importURIHelper = new ImportURIHelper(this);

        cardIdFieldView = findViewById(R.id.cardIdView);
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetButton = findViewById(R.id.bottomSheetButton);
        noteView = findViewById(R.id.noteView);
        groupsView = findViewById(R.id.groupsView);
        storeName = findViewById(R.id.storeName);
        barcodeImage = findViewById(R.id.barcode);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);

        rotationEnabled = true;

        // Allow making barcode fullscreen on tap
        barcodeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(barcodeIsFullscreen)
                {
                    setFullscreen(false);
                }
                else
                {
                    setFullscreen(true);
                }
            }
        });

        final FloatingActionButton editButton = findViewById(R.id.fabEdit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoyaltyCardEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", loyaltyCardId);
                bundle.putBoolean("update", true);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        final BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    editButton.hide();
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetButton.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
                    editButton.hide();
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetButton.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                    editButton.show();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        });

        bottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        Log.i(TAG, "Received new intent");
        extractIntentFields(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Log.i(TAG, "To view card: " + loyaltyCardId);

        if(barcodeIsFullscreen)
        {
            // Completely reset state
            //
            // This prevents the barcode from taking up the entire screen
            // on resume and thus being stretched out of proportion.
            recreate();
        }

        // The brightness value is on a scale from [0, ..., 1], where
        // '1' is the brightest. We attempt to maximize the brightness
        // to help barcode readers scan the barcode.
        Window window = getWindow();
        if(window != null && settings.useMaxBrightnessDisplayingBarcode())
        {
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.screenBrightness = 1F;
            window.setAttributes(attributes);
        }

        loyaltyCard = db.getLoyaltyCard(loyaltyCardId);
        if(loyaltyCard == null)
        {
            Log.w(TAG, "Could not lookup loyalty card " + loyaltyCardId);
            Toast.makeText(this, R.string.noCardExistsError, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String formatString = loyaltyCard.barcodeType;
        format = !formatString.isEmpty() ? BarcodeFormat.valueOf(formatString) : null;
        cardIdString = loyaltyCard.cardId;

        cardIdFieldView.setText(loyaltyCard.cardId);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(cardIdFieldView,
                getResources().getInteger(R.integer.settings_card_id_min_font_size_sp)-1, settings.getCardIdFontSize(),
                1, TypedValue.COMPLEX_UNIT_SP);

        if(loyaltyCard.note.length() > 0)
        {
            noteView.setVisibility(View.VISIBLE);
            noteView.setText(loyaltyCard.note);
        }
        else
        {
            noteView.setVisibility(View.GONE);
        }

        List<Group> loyaltyCardGroups = db.getLoyaltyCardGroups(loyaltyCardId);

        if(loyaltyCardGroups.size() > 0) {
            StringBuilder groupsString = new StringBuilder();
            for (Group group : loyaltyCardGroups) {
                groupsString.append(group._id);
                groupsString.append(" ");
            }

            groupsView.setVisibility(View.VISIBLE);
            groupsView.setText(getString(R.string.groupsList, groupsString.toString()));
        }
        else
        {
            groupsView.setVisibility(View.GONE);
        }

        if (noteView.getVisibility() == View.VISIBLE || groupsView.getVisibility() == View.VISIBLE) {
            bottomSheet.setVisibility(View.VISIBLE);
        }
        else
        {
            bottomSheet.setVisibility(View.GONE);
        }

        storeName.setText(loyaltyCard.store);
        storeName.setTextSize(settings.getCardTitleFontSize());

        // TODO: Grab color from image
        int backgroundHeaderColor;

        if (loyaltyCard.headerColor != null) {
            backgroundHeaderColor = loyaltyCard.headerColor;
        } else {
            backgroundHeaderColor = LetterBitmap.getDefaultColor(this, loyaltyCard.store);
        }

        collapsingToolbarLayout.setBackgroundColor(backgroundHeaderColor);

        int textColor;
        if(Utils.needsDarkForeground(backgroundHeaderColor))
        {
            textColor = Color.BLACK;
        }
        else
        {
            textColor = Color.WHITE;
        }
        storeName.setTextColor(textColor);

        // If the background is very bright, we should use dark icons
        backgroundNeedsDarkIcons = Utils.needsDarkForeground(backgroundHeaderColor);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setHomeAsUpIndicator(getIcon(R.drawable.ic_arrow_back_white, backgroundNeedsDarkIcons));
        }

        // Make notification area light if dark icons are needed
        if(Build.VERSION.SDK_INT >= 23)
        {
            window.getDecorView().setSystemUiVisibility(backgroundNeedsDarkIcons ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0);
        }
        if(Build.VERSION.SDK_INT >= 21)
        {
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // Set shadow colour of store text so even same color on same color would be readable
        storeName.setShadowLayer(1, 1, 1, backgroundNeedsDarkIcons ? Color.WHITE : Color.BLACK);

        if(format != null)
        {
            findViewById(R.id.barcode).setVisibility(View.VISIBLE);
            if(barcodeImage.getHeight() == 0)
            {
                Log.d(TAG, "ImageView size is not known known at start, waiting for load");
                // The size of the ImageView is not yet available as it has not
                // yet been drawn. Wait for it to be drawn so the size is available.
                barcodeImage.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener()
                        {
                            @Override
                            public void onGlobalLayout()
                            {
                                barcodeImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                                Log.d(TAG, "ImageView size now known");
                                new BarcodeImageWriterTask(barcodeImage, cardIdString, format).execute();
                            }
                        });
            }
            else
            {
                Log.d(TAG, "ImageView size known known, creating barcode");
                new BarcodeImageWriterTask(barcodeImage, cardIdString, format).execute();
            }
        }
        else
        {
            findViewById(R.id.barcode).setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (barcodeIsFullscreen)
        {
            setFullscreen(false);
            return;
        }

        super.onBackPressed();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.card_view_menu, menu);

        // Always calculate lockscreen icon, it may need a black color
        boolean lockBarcodeScreenOrientation = settings.getLockBarcodeScreenOrientation();
        MenuItem item = menu.findItem(R.id.action_lock_unlock);
        setOrientatonLock(item, lockBarcodeScreenOrientation);
        if(lockBarcodeScreenOrientation)
        {
            item.setVisible(false);
        }

        loyaltyCard = db.getLoyaltyCard(loyaltyCardId);
        starred = loyaltyCard.starStatus != 0;

        menu.findItem(R.id.action_share).setIcon(getIcon(R.drawable.ic_share_white, backgroundNeedsDarkIcons));

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (starred) {
            menu.findItem(R.id.action_star_unstar).setIcon(getIcon(R.drawable.ic_starred_white, backgroundNeedsDarkIcons));
            menu.findItem(R.id.action_star_unstar).setTitle(R.string.unstar);
        }
        else {
            menu.findItem(R.id.action_star_unstar).setIcon(getIcon(R.drawable.ic_unstarred_white, backgroundNeedsDarkIcons));
            menu.findItem(R.id.action_star_unstar).setTitle(R.string.star);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch(id)
        {
            case android.R.id.home:
                finish();
                break;

            case R.id.action_share:
                importURIHelper.startShareIntent(loyaltyCard);
                return true;

            case R.id.action_lock_unlock:
                if(rotationEnabled)
                {
                    setOrientatonLock(item, true);
                }
                else
                {
                    setOrientatonLock(item, false);
                }
                rotationEnabled = !rotationEnabled;
                return true;

            case R.id.action_star_unstar:
                starred = !starred;
                db.updateLoyaltyCardStarStatus(loyaltyCardId, starred ? 1 : 0);
                invalidateOptionsMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setOrientatonLock(MenuItem item, boolean lock)
    {
        if(lock)
        {

            item.setIcon(getIcon(R.drawable.ic_lock_outline_white_24dp, backgroundNeedsDarkIcons));
            item.setTitle(R.string.unlockScreen);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }
        else
        {
            item.setIcon(getIcon(R.drawable.ic_lock_open_white_24dp, backgroundNeedsDarkIcons));
            item.setTitle(R.string.lockScreen);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    /**
     * When enabled, hides the status bar and moves the barcode to the top of the screen.
     *
     * The purpose of this function is to make sure the barcode can be scanned from the phone
     * by machines which offer no space to insert the complete device.
     */
    private void setFullscreen(boolean enable)
    {
        ActionBar actionBar = getSupportActionBar();
        if(enable && !barcodeIsFullscreen)
        {
            // Save previous barcodeImage state
            barcodeImageState = barcodeImage.getLayoutParams();

            // Hide actionbar
            if(actionBar != null)
            {
                actionBar.hide();
            }

            // Hide collapsingToolbar
            collapsingToolbarLayout.setVisibility(View.GONE);

            // Set Android to fullscreen mode
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
            );

            // Make barcode take all space
            barcodeImage.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            ));

            // Move barcode to top
            barcodeImage.setScaleType(ImageView.ScaleType.FIT_START);

            // Prevent centering
            barcodeImage.setAdjustViewBounds(false);

            // Set current state
            barcodeIsFullscreen = true;
        }
        else if(!enable && barcodeIsFullscreen)
        {
            // Show actionbar
            if(actionBar != null)
            {
                actionBar.show();
            }

            // Show collapsingToolbar
            collapsingToolbarLayout.setVisibility(View.VISIBLE);

            // Unset fullscreen mode
            getWindow().getDecorView().setSystemUiVisibility(
                getWindow().getDecorView().getSystemUiVisibility()
                & ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                & ~View.SYSTEM_UI_FLAG_FULLSCREEN
            );

            // Turn barcode back to normal
            barcodeImage.setLayoutParams(barcodeImageState);

            // Fix barcode centering
            barcodeImage.setAdjustViewBounds(true);

            // Set current state
            barcodeIsFullscreen = false;
        }
    }
}
