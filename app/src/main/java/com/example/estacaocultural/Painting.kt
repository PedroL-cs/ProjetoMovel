package com.example.estacaocultural

import android.os.Parcel
import android.os.Parcelable

data class Painting(
    val nomeObra: String = "",
    val autor: String = "",
    val ano: String = "",
    val descricao: String = "",
    val imagem: String = "",
    val id: String = "",
    val qrCode: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nomeObra)
        parcel.writeString(autor)
        parcel.writeString(ano)
        parcel.writeString(descricao)
        parcel.writeString(imagem)
        parcel.writeString(id)
        parcel.writeString(qrCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Painting> {
        override fun createFromParcel(parcel: Parcel): Painting {
            return Painting(parcel)
        }

        override fun newArray(size: Int): Array<Painting?> {
            return arrayOfNulls(size)
        }
    }
}

