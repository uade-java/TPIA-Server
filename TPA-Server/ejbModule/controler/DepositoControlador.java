package controler;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import bean.ArticuloBean;
import bean.ItemRecepcionCompraBean;
import bean.RecepcionCompraBean;
import bean.SolicitudArticuloBean;
import bean.SolicitudCompraBean;
import dao.ArticuloDao;
import dto.ArticuloDTO;
import dto.ItemSolicitudCompraDTO;
import dto.RecepcionCompraDTO;
import dto.SolicitudArticuloDTO;
import dto.SolicitudCompraDTO;
import util.Utils;


/**
 * Desde este controlador se deberian de llamar a todos los servicios
 * implementados.
 * 
 * @author Martin
 *
 */
@Stateless
@LocalBean
public class DepositoControlador implements IDepositoControladorLocal, IDepositoControladorRemote{

	@PersistenceContext(unitName="MyPU")
	private EntityManager em;
	
	public static DepositoControlador instancia;

	// Singleton
	
	public static DepositoControlador getInstancia() {
		if (instancia == null)
			return new DepositoControlador();
		return instancia;
	}

	// Constructor
	
	public DepositoControlador() {}

	// M�todos a implementar
	
	@Override
	public List <SolicitudArticuloDTO> listarSolicitudArticuloPendiente() {
		
		Query q = em.createQuery("Select s from SolicitudArticuloBean S where s.estado =:estado").setParameter("estado", "pendiente");
		@SuppressWarnings("unchecked")
		List<SolicitudArticuloBean> salida = q.getResultList();
		return Utils.solicitudArticuloBeanToDTO(salida);
		
	}
	
	@Override
	public void crearArticulo(ArticuloDTO articuloDTO){
		
		ArticuloBean newArticulo = new ArticuloBean();
		newArticulo.aArticuloBean(articuloDTO);
		em.persist(newArticulo);
	}
	
		
	@Override
	public void modificarStockDelArticulo(ArticuloDTO articuloDTO){
		
//		ArticuloBean newArticulo = buscarArticuloPorCodigo(articuloDTO.getCodArticulo());		
//		newArticulo.aArticuloBean(articuloDTO);
//		em.merge(newArticulo);	
		
		ArticuloBean newArticulo = ArticuloDao.getInstancia().buscarArticuloPorCodigo(108);
		newArticulo.aArticuloBean(articuloDTO);
		em.merge(newArticulo);
		
	}


	@Override
	public void modificarArticulo(ArticuloDTO articuloDTO) {
		
		ArticuloBean newArticulo = new ArticuloBean();
		newArticulo.aArticuloBean(articuloDTO);
		em.merge(newArticulo);
	}


	@Override
	public void crearSolicitudCompra(SolicitudCompraDTO compraDTO){

		SolicitudCompraBean newSolicitudCompraBean = new SolicitudCompraBean();
		newSolicitudCompraBean.aSolicitudCompraBean(compraDTO);
		em.persist(newSolicitudCompraBean);
	}


	@Override
	public void registrarRecepcionCompra(RecepcionCompraDTO compraDTO) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<SolicitudArticuloDTO> listarSolicitudesPendientes() {
		//Aca hacer DTO->Bean
		
		return null;
	}

	
	@Override
	public void crearRecepcionCompra(SolicitudCompraDTO solCompraDTO) {
		
		//Convertimos la solicitud de compra DTO a Recepci�n de compra BEAN

		RecepcionCompraBean recepCompra = new RecepcionCompraBean();		
		recepCompra.setCodigo(solCompraDTO.getCodigo());										// Seteamos el codigo de la recepci�n de compra
		List<ItemRecepcionCompraBean> itemsRecepCompra = new ArrayList<ItemRecepcionCompraBean>(); 		// Creamos la lista de Items de recepci�n de compra
		
		for (ItemSolicitudCompraDTO itSolDTO : solCompraDTO.getItemsSolicitudesCompra()) {		// Recorremos los items de la solicitud de compra a convertir en items recepcion
			
			// Obtenemos el articulo de la db por el codigo
			ArticuloBean art; 
			art = (ArticuloBean) em.createQuery("select a from ArticuloBean a where a.codArticulo = :codArticulo")
			.setParameter("codArticulo", itSolDTO.getArticulo().getCodArticulo())
			.getSingleResult();
			
			// Creamos el Items de la recepci�n de compra y seteamos articulo y cantidad
			ItemRecepcionCompraBean itRecepCompra = new ItemRecepcionCompraBean();
			itRecepCompra.setArticulo(art);
			itRecepCompra.setCantidad(itSolDTO.getCantidad());
			
			// Agregamos el item creado al array
			itemsRecepCompra.add(itRecepCompra);
			
			//Actualiza Stock
			Integer newStock = art.getCantidadDisponible() + itRecepCompra.getCantidad();
			art.setCantidadDisponible(newStock);
			em.merge(art);			
		}
		
		recepCompra.setItemsRecepcionesCompra(itemsRecepCompra);			// Seteamos la lista de items de recepci�n al bean de recepcion de compra
		em.persist(recepCompra);									// persistimos la recepci�n de compra
		
	}

	
	@Override
	public void crearSolicitudArticulo(SolicitudArticuloDTO solicitudArticuloDTO) {
			
		SolicitudArticuloBean newSolicitudArticuloBean = new SolicitudArticuloBean();
		newSolicitudArticuloBean.aSolicitudArticuloBean(solicitudArticuloDTO);
		em.persist(newSolicitudArticuloBean);		
	}

	
	
	// Estos m�todos de abajo deber�an ser privados y no tienen que estar en la interface
	
	public ArticuloBean buscarArticuloPorCodigo(Integer codArticulo) {
		
		return (ArticuloBean) em.createQuery("SELECT a FROM ArticuloBean a where a.codArticulo= :codArticulo").setParameter("codArticulo", codArticulo).getSingleResult();		
	}

	public ArticuloDTO buscarArticuloPorNombre(String nombre) {
		
		return (ArticuloDTO) em.createQuery("SELECT a FROM ArticuloBean a where a.nombre = :nombre").setParameter("nombre", nombre).getSingleResult();			
	}	
	
	@SuppressWarnings("unchecked")
	public List<ArticuloDTO> listarArticulos() {
		
		Query q = em.createQuery("from ArticuloBean");
		@SuppressWarnings("unused")
		List<ArticuloBean> salida = new ArrayList<ArticuloBean>();
		salida = q.getResultList();
		//return salida.stream().map(articuloBean -> new ArticuloDTO(a).collect(Collectors.<ArticuloDTO>toList()));
		return null;
	}



}
