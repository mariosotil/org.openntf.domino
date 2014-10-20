package org.openntf.domino.graph2.annotations;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.tinkerpop.frames.structures.FramedVertexIterable;

public class AdjacencyUniqueHandler implements AnnotationHandler<AdjacencyUnique> {

	@Override
	public Class<AdjacencyUnique> getAnnotationType() {
		return AdjacencyUnique.class;
	}

	@Override
	public Object processElement(final AdjacencyUnique annotation, final Method method, final Object[] arguments,
			final FramedGraph framedGraph, final Element element, final Direction direction) {
		if (element instanceof Vertex) {
			return processVertex(annotation, method, arguments, framedGraph, (Vertex) element);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public Object processVertex(final AdjacencyUnique adjacency, final Method method, final Object[] arguments,
			final FramedGraph framedGraph, final Vertex vertex) {
		if (ClassUtilities.isGetMethod(method)) {
			final FramedVertexIterable r = new FramedVertexIterable(framedGraph, vertex.getVertices(adjacency.direction(),
					adjacency.label()), ClassUtilities.getGenericClass(method));
			if (ClassUtilities.returnsIterable(method)) {
				return r;
			} else {
				return r.iterator().hasNext() ? r.iterator().next() : null;
			}
		} else if (ClassUtilities.isAddMethod(method)) {
			Class<?> returnType = method.getReturnType();
			Vertex newVertex;
			Object returnValue = null;
			if (arguments == null) {
				// Use this method to get the vertex so that the vertex
				// initializer is called.
				returnValue = framedGraph.addVertex(null, returnType);
				newVertex = ((VertexFrame) returnValue).asVertex();
			} else {
				newVertex = ((VertexFrame) arguments[0]).asVertex();
				switch (adjacency.direction()) {
				case OUT:
					Vertex outVertex = vertex;
					Vertex inVertex = newVertex;
					Iterable<Edge> outedges = outVertex.getEdges(Direction.OUT, adjacency.label());	//FIXME NTF Correct direction?
					for (Edge edge : outedges) {
						Vertex v = edge.getVertex(Direction.IN);
						if (v.getId().equals(inVertex.getId())) {
							return framedGraph.frame(edge, method.getReturnType());
						}
					}
					break;
				case IN:
					outVertex = newVertex;
					inVertex = vertex;
					Iterable<Edge> inedges = outVertex.getEdges(Direction.IN, adjacency.label());	//FIXME NTF Correct direction?
					for (Edge edge : inedges) {
						Vertex v = edge.getVertex(Direction.IN);
						if (v.getId().equals(inVertex.getId())) {
							return framedGraph.frame(edge, method.getReturnType());
						}
					}
					break;
				default:
					throw new UnsupportedOperationException("Direction.BOTH it not supported on 'add' or 'set' methods");
				}
			}
			addEdges(adjacency, framedGraph, vertex, newVertex);

			if (returnType.isPrimitive()) {
				return null;
			} else {
				return returnValue;
			}

		} else if (ClassUtilities.isRemoveMethod(method)) {
			removeEdges(adjacency.direction(), adjacency.label(), vertex, ((VertexFrame) arguments[0]).asVertex(), framedGraph);
			return null;
		} else if (ClassUtilities.isSetMethod(method)) {
			removeEdges(adjacency.direction(), adjacency.label(), vertex, null, framedGraph);
			if (ClassUtilities.acceptsIterable(method)) {
				for (Object o : (Iterable) arguments[0]) {
					Vertex v = ((VertexFrame) o).asVertex();
					addEdges(adjacency, framedGraph, vertex, v);
				}
				return null;
			} else {
				if (null != arguments[0]) {
					Vertex newVertex = ((VertexFrame) arguments[0]).asVertex();
					addEdges(adjacency, framedGraph, vertex, newVertex);
				}
				return null;
			}
		}

		return null;
	}

	private void addEdges(final AdjacencyUnique adjacency, final FramedGraph framedGraph, final Vertex vertex, final Vertex newVertex) {
		boolean exists = false;
		switch (adjacency.direction()) {
		case OUT:
			Iterable<Edge> outedges = vertex.getEdges(Direction.OUT, adjacency.label());	//FIXME NTF Correct direction?
			for (Edge edge : outedges) {
				Vertex v = edge.getVertex(Direction.IN);
				if (v.getId().equals(newVertex.getId())) {
					exists = true;
					break;
				}
			}
			if (!exists)
				framedGraph.addEdge(null, vertex, newVertex, adjacency.label());
			break;
		case IN:
			Iterable<Edge> inedges = vertex.getEdges(Direction.IN, adjacency.label());	//FIXME NTF Correct direction?
			for (Edge edge : inedges) {
				Vertex v = edge.getVertex(Direction.OUT);
				if (v.getId().equals(newVertex.getId())) {
					exists = true;
					break;
				}
			}
			if (!exists)
				framedGraph.addEdge(null, newVertex, vertex, adjacency.label());
			break;
		case BOTH:
			throw new UnsupportedOperationException("Direction.BOTH it not supported on 'add' or 'set' methods");
		}
	}

	private void removeEdges(final Direction direction, final String label, final Vertex element, final Vertex otherVertex,
			final FramedGraph framedGraph) {
		for (final Edge edge : element.getEdges(direction, label)) {
			if (null == otherVertex || edge.getVertex(direction.opposite()).equals(otherVertex)) {
				framedGraph.removeEdge(edge);
			}
		}
	}
}