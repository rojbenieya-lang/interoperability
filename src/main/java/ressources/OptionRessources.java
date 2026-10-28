package ressources;


import entities.Option;
import filter.Secured;
import metiers.OptionBusiness;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Secured
@Path("/options")
public class OptionRessources {

    public static OptionBusiness optionMetier = new OptionBusiness();

    @POST
    @Consumes("application/json")
    public Response addOption(Option option) {
        if (optionMetier.addOption(option))
            return Response.status(Response.Status.CREATED).build();
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }

    @GET
    @Produces("application/json")
    public Response getOptions(@QueryParam("domaine") String domaine) {
        List<Option> liste = new ArrayList<>();

        if (domaine != null) {
            liste = optionMetier.getOptionsByDomaine(domaine);
        } else {
            liste = optionMetier.getListeOptions();
        }

        if (liste.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.status(Response.Status.OK).entity(liste).build();
    }

    @PUT
    @Path("/{codeOption}")
    @Consumes("application/json")
    public Response updateOption(Option updatedOption, @PathParam("codeOption") int codeOption) {
        if (optionMetier.updateOption(codeOption, updatedOption)) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{codeOption}")
    public Response deleteOption(@PathParam("codeOption") int codeOption) {
        if (optionMetier.deleteOption(codeOption))
            return Response.status(Response.Status.OK).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/{codeOption}")
    @Produces("application/json")
    public Response getOptionById(@PathParam("codeOption") int codeOption) {
        Option option = optionMetier.getOptionByCode(codeOption);
        if (option != null)
            return Response.status(Response.Status.OK).entity(option).build();
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}