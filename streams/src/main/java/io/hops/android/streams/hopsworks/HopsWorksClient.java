package io.hops.android.streams.hopsworks;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HopsWorksClient {

    @Headers({"User-Agent: Android"})
    @POST("{projectName}/register")
    Call<HopsWorksResponse> register(
            @Path(value = "projectName") String projectName, @Body DeviceDTO deviceInfo);

    @POST("{projectName}/login")
    Call<HopsWorksResponse> login(@Path(value = "projectName") String projectName,
                                  @Body DeviceDTO deviceInfo);

    @POST("{projectName}/verify-token")
    Call<HopsWorksResponse> verifyToken(@Path(value = "projectName") String projectName,
                                        @Header("Authorization") String jwtToken,
                                        @Body DeviceDTO deviceInfo);

    @GET("{projectName}/topic-schema")
    Call<SchemaDTO> getTopicSchema(@Path(value = "projectName") String projectName,
                                   @Header("Authorization") String jwtToken,
                                   @Query("topic") String topicName);

    @POST("{projectName}/produce")
    Call<List<AckRecordDTO>> produce(@Path(value = "projectName") String projectName,
                                     @Header("Authorization") String jwtToken,
                                     @Body TopicRecordsDTO topicRecordsDTO);

}