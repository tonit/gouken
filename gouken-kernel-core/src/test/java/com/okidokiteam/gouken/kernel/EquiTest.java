package com.okidokiteam.gouken.kernel;import org.junit.Test;import static org.hamcrest.core.Is.*;import static org.junit.Assert.*;/** * Equilibrium Index Test */public class EquiTest{    private int equi( int[] A )    {        return 0;    }    @Test    public void testMe()    {        assertThat( equi( new int[]{ } ), is( 0 ) );        assertThat( equi( new int[]{ 1 } ), is( 0 ) );        assertThat( equi( new int[]{ 1,-1,5} ), is( 0 ) );    }}