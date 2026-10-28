package ressources;


import entities.Etudiant;
import entities.EtudiantList;
import entities.Option;
import filter.Secured;
import metiers.EtudiantBusiness;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Secured
@Path("etudiants")
public class EtudiantRessources {
    public static EtudiantBusiness etudiantMetier = new EtudiantBusiness();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEtudiant(Etudiant etudiant) {
        if (etudiantMetier.addEtudiant(etudiant)) {
            return Response.status(Response.Status.CREATED).entity(etudiantMetier.getAllEtudiants()).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_XML)
    public Response getAllEtudiant() {
        if (etudiantMetier.getAllEtudiants().size() != 0) {
            return Response.status(Response.Status.OK).entity(new EtudiantList(etudiantMetier.getAllEtudiants())).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    @GET
    @Path("/{identifiant}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEtudiantById(@PathParam("identifiant") String identifiant) {
        Etudiant etudiant = etudiantMetier.getEtudiantByIdentifiant(identifiant);
        if (etudiant != null) {
            return Response.status(Response.Status.OK).entity(etudiant).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllEtudiants(@QueryParam("option") int codeOption) {
        Option option = new Option();
        option.setCodeOption(codeOption);
        List<Etudiant> etudiants;

        if (codeOption != 0) {
            etudiants = etudiantMetier.getEtudiantsByOption(option);
        } else {
            etudiants = etudiantMetier.getAllEtudiants();
        }

        if (etudiants.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.OK).entity(etudiants).build();
    }

    @PUT
    @Path("/{identifiant}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateEtudiant(@PathParam("identifiant") String identifiant, Etudiant updatedEtudiant) {
        if (etudiantMetier.updateEtudiant(identifiant, updatedEtudiant)) {
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{identifiant}")
    public Response deleteEtudiant(@PathParam("identifiant") String identifiant) {
        if (etudiantMetier.deleteEtudiant(identifiant)) {
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/option")
    @Produces(MediaType.APPLICATION_XML)
    public Response getEtudiantsByOption(@QueryParam("codeOption") int codeOption) {
        Option option = new Option();
        option.setCodeOption(codeOption);
        List<Etudiant> etudiants = etudiantMetier.getEtudiantsByOption(option);

        if (etudiants.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.OK).entity(new EtudiantList(etudiants)).build();
    }
}