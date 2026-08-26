package com.example.gtj_f230_rebound_corrosion;
import com.example.gtj_f230_rebound_corrosion.IRemoteCallback_GTJ;
interface IRemoteService_GTJ {
     void register(in String pkgName,in IRemoteCallback_GTJ callback);
     void unRegister(in String pkgName,in IRemoteCallback_GTJ callback);
     void send(in String packageName,in String func,in String params);
}
