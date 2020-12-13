package protect.card_locker;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import protect.card_locker.preferences.Settings;

class LoyaltyCardCursorAdapter extends CursorAdapter
{
    Settings settings;

    public LoyaltyCardCursorAdapter(Context context, Cursor cursor)
    {
        super(context, cursor, 0);
        settings = new Settings(context);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        return LayoutInflater.from(context).inflate(R.layout.loyalty_card_layout, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        // Find fields to populate in inflated template
        ImageView thumbnail = view.findViewById(R.id.thumbnail);
        TextView storeField = view.findViewById(R.id.store);
        TextView noteField = view.findViewById(R.id.note);
        ImageView star = view.findViewById(R.id.star);

        // Extract properties from cursor
        LoyaltyCard loyaltyCard = LoyaltyCard.toLoyaltyCard(cursor);

        // Populate fields with extracted properties
        storeField.setText(loyaltyCard.store);

        storeField.setTextSize(settings.getCardTitleListFontSize());

        if(!loyaltyCard.note.isEmpty())
        {
            noteField.setVisibility(View.VISIBLE);
            noteField.setText(loyaltyCard.note);
            noteField.setTextSize(settings.getCardNoteListFontSize());
        }
        else
        {
            noteField.setVisibility(View.GONE);
        }

        if (loyaltyCard.starStatus!=0)
        {
            star.setVisibility(View.VISIBLE);
        }
        else
        {
            star.setVisibility(View.GONE);
        }

        if (loyaltyCard.icon != null) {
            thumbnail.setImageBitmap(loyaltyCard.icon);
        }
        else
        {
            thumbnail.setImageBitmap(Utils.generateIcon(context, loyaltyCard.store, loyaltyCard.headerColor).getLetterTile());
        }
    }
}
