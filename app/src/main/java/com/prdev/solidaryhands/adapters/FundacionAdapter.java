package com.prdev.solidaryhands.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prdev.solidaryhands.FundacionActivity;
import com.prdev.solidaryhands.R;
import com.prdev.solidaryhands.models.Fundacion;

import java.util.ArrayList;
import java.util.List;

public class FundacionAdapter extends RecyclerView.Adapter<FundacionAdapter.FundacionViewHolder> {

    private final Context context;
    private final List<Fundacion> listaCompleta;
    private final List<Fundacion> listaFiltrada;

    public FundacionAdapter(Context context, List<Fundacion> fundaciones) {
        this.context = context;
        this.listaCompleta = new ArrayList<>(fundaciones);
        this.listaFiltrada = new ArrayList<>(fundaciones);
    }

    public void actualizarDatos(List<Fundacion> nuevasFundaciones) {
        listaCompleta.clear();
        listaFiltrada.clear();
        if (nuevasFundaciones != null) {
            listaCompleta.addAll(nuevasFundaciones);
            listaFiltrada.addAll(nuevasFundaciones);
        }
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        listaFiltrada.clear();
        if (texto == null || texto.trim().isEmpty()) {
            listaFiltrada.addAll(listaCompleta);
        } else {
            String busqueda = texto.toLowerCase().trim();
            for (Fundacion f : listaCompleta) {
                if (f.getNombre().toLowerCase().contains(busqueda)
                        || f.getDescripcion().toLowerCase().contains(busqueda)) {
                    listaFiltrada.add(f);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FundacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fundacion_card, parent, false);
        return new FundacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FundacionViewHolder holder, int position) {
        Fundacion fundacion = listaFiltrada.get(position);
        holder.txtNombre.setText(fundacion.getNombre());
        holder.txtDescripcion.setText(fundacion.getDescripcion());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FundacionActivity.class);
            intent.putExtra("nombreFundacion", fundacion.getNombre());
            intent.putExtra("fundacionObjeto", fundacion);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public static class FundacionViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre;
        TextView txtDescripcion;
        ImageView imgLogo;

        public FundacionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreFundacionItem);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionFundacionItem);
            imgLogo = itemView.findViewById(R.id.imgLogoFundacion);
        }
    }
}
