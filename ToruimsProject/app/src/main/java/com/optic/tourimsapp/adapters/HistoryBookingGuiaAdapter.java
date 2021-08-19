package com.optic.tourimsapp.adapters;

import android.content.Context;
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
import com.optic.tourimsapp.modelos.HistorialBooking;
import com.optic.tourimsapp.providers.GuiaTuristicoProvider;
import com.optic.tourimsapp.providers.TuristaProvider;
import com.squareup.picasso.Picasso;

public class HistoryBookingGuiaAdapter extends FirebaseRecyclerAdapter<HistorialBooking, HistoryBookingGuiaAdapter.ViewHolder> {

    private TuristaProvider mTuristaProvider;
    private Context mContext ;

    public HistoryBookingGuiaAdapter(FirebaseRecyclerOptions<HistorialBooking> options, Context context){
        super(options);
        mTuristaProvider = new TuristaProvider();
        mContext = context;

    }

    @Override
    protected void onBindViewHolder(@NonNull HistoryBookingGuiaAdapter.ViewHolder holder, int position, @NonNull HistorialBooking historyBooking) {
        holder.txtViewOrigen.setText(historyBooking.getOrigen());
        holder.txtViewDestino.setText(historyBooking.getDestino());
        holder.txtViewCalificacion.setText(String.valueOf(historyBooking.getCalificacionGuia()));
        mTuristaProvider.obtenerCliente(historyBooking.getIdTurista()).addListenerForSingleValueEvent(new ValueEventListener() {
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



    }

    @NonNull
    @Override
    public HistoryBookingGuiaAdapter.ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking, parent,false);
        return new HistoryBookingGuiaAdapter.ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtViewNombre;
        private TextView txtViewOrigen;
        private TextView txtViewDestino;
        private TextView txtViewCalificacion;
        private ImageView imageViewHistoryBooking;
        public ViewHolder(View view) {
            super(view);
            txtViewNombre = view.findViewById(R.id.txViewNombre);
            txtViewOrigen = view.findViewById(R.id.txtViewOrigen);
            txtViewDestino = view.findViewById(R.id.txtViewDestino);
            txtViewCalificacion = view.findViewById(R.id.txtViewCalificacion);
            imageViewHistoryBooking = view.findViewById(R.id.imageViewHistoryBooking);

        }
    }
}
