package com.optic.tourimsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.Turistas.HistoryBookingDetailTuristaActivity;
import com.optic.tourimsapp.modelos.HistorialBooking;
import com.optic.tourimsapp.providers.GuiaTuristicoProvider;
import com.squareup.picasso.Picasso;

public class HistoryBookingTuristaAdapter extends FirebaseRecyclerAdapter<HistorialBooking, HistoryBookingTuristaAdapter.ViewHolder> {

    private GuiaTuristicoProvider mGuiaProvider;
    private Context mContext ;

    public HistoryBookingTuristaAdapter(FirebaseRecyclerOptions<HistorialBooking> options, Context context){
        super(options);
        mGuiaProvider = new GuiaTuristicoProvider();
        mContext = context;

    }

    @Override
    protected void onBindViewHolder(@NonNull HistoryBookingTuristaAdapter.ViewHolder holder, int position, @NonNull HistorialBooking historyBooking) {

        String id = getRef(position).getKey();
        holder.txtViewOrigen.setText(historyBooking.getOrigen());
        holder.txtViewDestino.setText(historyBooking.getDestino());
        holder.txtViewCalificacion.setText(String.valueOf(historyBooking.getCalificacionTurista()));
        mGuiaProvider.obtenerGuia(historyBooking.getIdGuiaTuristico()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombreCompleto = snapshot.child("nombreCompleto").getValue().toString();
                    holder.txtViewNombre.setText(nombreCompleto);
                    if(snapshot.hasChild("Imagen")){
                        String imagen = snapshot.child("Imagen").getValue().toString();
                        Picasso.with(mContext).load(imagen).into(holder.imageViewHistoryBooking);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, HistoryBookingDetailTuristaActivity.class);
                intent.putExtra("idHistoryBooking", id);
                mContext.startActivity(intent);
            }
        });



    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking, parent,false);
        return new HistoryBookingTuristaAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtViewNombre;
        private TextView txtViewOrigen;
        private TextView txtViewDestino;
        private TextView txtViewCalificacion;
        private ImageView imageViewHistoryBooking;
        private View view;


        public ViewHolder(View view) {
            super(view);
            this.view = view;
            txtViewNombre = view.findViewById(R.id.txViewNombre);
            txtViewOrigen = view.findViewById(R.id.txtViewOrigen);
            txtViewDestino = view.findViewById(R.id.txtViewDestino);
            txtViewCalificacion = view.findViewById(R.id.txtViewCalificacion);
            imageViewHistoryBooking = view.findViewById(R.id.imageViewHistoryBooking);

        }
    }
}
