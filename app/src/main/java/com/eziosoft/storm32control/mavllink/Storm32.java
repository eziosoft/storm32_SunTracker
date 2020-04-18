/*
 * * Copyright (C) Bartosz Szczygiel - All Rights Reserved
 *  * Unauthorized copying of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by Bartosz Szczygiel <eziosoft@gmail.com>
 *
 */

package com.eziosoft.storm32control.mavllink;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Storm32 {
    final int startSignIncoming = 0xFA;
    final int startSignOutgoing = 0xFB;

    final int CMD_SETPITCHROLLYAW = 18;
    //0xFA 0x06 0x12 data1-low-byte data1-high-byte data2-low-byte data2-high-byte data3-low-byte data3-high-byte crc-low-byte crc-high-byte
    //The data1, data2 and data3 are each of type uint16_t and can assume the values 700...2300. They represent the pitch, roll, yaw input values. If a value 0 is send, then the respective axis will be recentered. Any other values are ignored. As response to this command a CMD_ACK message will be emitted.


    final int CMD_SETANGLE = 17;
    //0xFA 0x0E 0x11 float1 float2 float3 flags-byte type-byte crc-low-byte crc-high-byte
    //The float1, float2, float3 fields represent 4 bytes each. They are of type float, and correspond to the pitch, roll,
    // and yaw angles in degree. The flags byte allows to modify the behavior of the angle setting for each angle.
    // It can be in the limited or unlimited mode. In limited mode the angle setting is subject to the RcMin and RcMax settings,
    // and works only for "absolute". In unlimited mode any angle value can be set without restriction, bypassing the RcMin and RcMax settings,
    // and works for both "relative" and "absolute". The first bit, 0x01, corresponds to pitch, 0x02 to roll, and 0x04 to yaw, and when set the
    // respective axis is in limited mode. The type byte is not used currently and has to be set to zero. As response to this command a CMD_ACK
    // message will be emitted.


    List<Byte> SendSTORM32Command(int command, Character[] payload) {
        List<Byte> bf = new LinkedList<>();
        int crc = MAVLinkCRC.crc_init();

        bf.add((byte) startSignIncoming);
        bf.add((byte) payload.length);
        crc = MAVLinkCRC.crc_accumulate((byte) payload.length, crc);
        bf.add((byte) command);
        crc = MAVLinkCRC.crc_accumulate((byte) command, crc);

        if (payload != null) {
            for (char c : payload) {
                bf.add((byte) (c & 0xFF));
                crc = MAVLinkCRC.crc_accumulate((byte) (c & 0xFF), crc);
            }
        }

        bf.add((byte) (crc & 0x00FF));
        bf.add((byte) ((crc >> 8) & 0x00FF));


        return bf;
    }


    public byte[] setCameraAngle(float pitch, float roll, float yaw) {

        if (pitch > 90) pitch = 90;
        if (pitch < -90) pitch = -90;

        if (roll > 20) roll = 20;
        if (roll < -20) roll = -20;

        if (roll > 20) roll = 20;
        if (roll < -20) roll = -20;

        // Log.d("Storm32", "Set Camera Angle : " + String.valueOf(pitch) + " " + String.valueOf(roll) + " " + String.valueOf(yaw) + " ");
        //0xFA 0x0E 0x11 float1 float2 float3 flags-byte type-byte crc-low-byte crc-high-byte
        ArrayList<Character> payload = new ArrayList<Character>();
        payload = new ArrayList<Character>();

        byte[] b = floatTobytes(pitch);
        payload.add((char) b[0]);
        payload.add((char) b[1]);
        payload.add((char) b[2]);
        payload.add((char) b[3]);

        b = floatTobytes(roll);
        payload.add((char) b[0]);
        payload.add((char) b[1]);
        payload.add((char) b[2]);
        payload.add((char) b[3]);

        b = floatTobytes(yaw);
        payload.add((char) b[0]);
        payload.add((char) b[1]);
        payload.add((char) b[2]);
        payload.add((char) b[3]);


        payload.add((char) 0);//flags
        payload.add((char) 0);//type - not used always 0
        List<Byte> bytes = SendSTORM32Command(CMD_SETANGLE, payload.toArray(new Character[payload.size()]));

        byte[] bbb = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bbb[i] = bytes.get(i).byteValue();
        }
        return bbb;

    }


    byte[] floatTobytes(float f) {
        int bits = Float.floatToIntBits(f);
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (bits & 0xff);
        bytes[1] = (byte) ((bits >> 8) & 0xff);
        bytes[2] = (byte) ((bits >> 16) & 0xff);
        bytes[3] = (byte) ((bits >> 24) & 0xff);
        return bytes;
    }


}
