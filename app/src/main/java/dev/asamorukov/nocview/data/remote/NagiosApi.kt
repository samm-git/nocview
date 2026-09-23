package dev.asamorukov.nocview.data.remote

import dev.asamorukov.nocview.data.remote.dto.CountData
import dev.asamorukov.nocview.data.remote.dto.Envelope
import dev.asamorukov.nocview.data.remote.dto.HostData
import dev.asamorukov.nocview.data.remote.dto.HostListData
import dev.asamorukov.nocview.data.remote.dto.ProgramStatusData
import dev.asamorukov.nocview.data.remote.dto.ServiceData
import dev.asamorukov.nocview.data.remote.dto.ServiceListData
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NagiosApi {

    @GET("cgi-bin/statusjson.cgi")
    suspend fun hostCount(
        @Query("query") query: String = "hostcount",
        @Query("formatoptions") formatOptions: String = "enumerate",
    ): Envelope<CountData>

    @GET("cgi-bin/statusjson.cgi")
    suspend fun serviceCount(
        @Query("query") query: String = "servicecount",
        @Query("formatoptions") formatOptions: String = "enumerate",
    ): Envelope<CountData>

    @GET("cgi-bin/statusjson.cgi")
    suspend fun hostList(
        @Query("query") query: String = "hostlist",
        @Query("details") details: Boolean = true,
        @Query("formatoptions") formatOptions: String = "enumerate",
    ): Envelope<HostListData>

    @GET("cgi-bin/statusjson.cgi")
    suspend fun serviceList(
        @Query("query") query: String = "servicelist",
        @Query("details") details: Boolean = true,
        @Query("formatoptions") formatOptions: String = "enumerate",
    ): Envelope<ServiceListData>

    @GET("cgi-bin/statusjson.cgi")
    suspend fun host(
        @Query("query") query: String = "host",
        @Query("hostname") hostname: String,
        @Query("formatoptions") formatOptions: String = "enumerate",
    ): Envelope<HostData>

    @GET("cgi-bin/statusjson.cgi")
    suspend fun service(
        @Query("query") query: String = "service",
        @Query("hostname") hostname: String,
        @Query("servicedescription") description: String,
        @Query("formatoptions") formatOptions: String = "enumerate",
    ): Envelope<ServiceData>

    @GET("cgi-bin/statusjson.cgi")
    suspend fun programStatus(
        @Query("query") query: String = "programstatus",
        @Query("formatoptions") formatOptions: String = "enumerate",
    ): Envelope<ProgramStatusData>

    @GET("cgi-bin/cmd.cgi")
    suspend fun commandForm(
        @Query("cmd_typ") cmdType: Int,
        @Query("host") host: String,
        @Query("service") service: String? = null,
    ): ResponseBody

    @FormUrlEncoded
    @POST("cgi-bin/cmd.cgi")
    suspend fun acknowledgeService(
        @Field("cmd_typ") cmdType: Int,
        @Field("cmd_mod") cmdMod: Int,
        @Field("nagFormId") nagFormId: String,
        @Field("host") host: String,
        @Field("service") service: String,
        @Field("com_author") author: String,
        @Field("com_data") comment: String,
        @Field("sticky_ack") sticky: Int,
        @Field("send_notification") notify: Int,
        @Field("persistent") persistent: Int,
    ): ResponseBody

    @FormUrlEncoded
    @POST("cgi-bin/cmd.cgi")
    suspend fun acknowledgeHost(
        @Field("cmd_typ") cmdType: Int,
        @Field("cmd_mod") cmdMod: Int,
        @Field("nagFormId") nagFormId: String,
        @Field("host") host: String,
        @Field("com_author") author: String,
        @Field("com_data") comment: String,
        @Field("sticky_ack") sticky: Int,
        @Field("send_notification") notify: Int,
        @Field("persistent") persistent: Int,
    ): ResponseBody
}
