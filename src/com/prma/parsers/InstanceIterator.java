/**
 * 
 */
package com.prma.parsers;

import com.prma.*;
import java.io.*;

/**
 * @author sharat
 *
 */
public interface InstanceIterator {
	public void Initialize(InputStream in);
	public boolean hasNextInstance();
	public Structures.Instance nextInstance();
}